package com.romarjozeka.app.ws.service.impl;

import com.romarjozeka.app.ws.exceptions.ResourceNotFoundException;
import com.romarjozeka.app.ws.io.entity.PasswordResetTokenEntity;
import com.romarjozeka.app.ws.io.entity.UserEntity;
import com.romarjozeka.app.ws.io.repository.PasswordResetTokenRepository;
import com.romarjozeka.app.ws.io.repository.UserRepository;
import com.romarjozeka.app.ws.service.UserService;
import com.romarjozeka.app.ws.shared.Utils;
import com.romarjozeka.app.ws.shared.aws.AmazonSES;
import com.romarjozeka.app.ws.shared.dto.AddressDto;
import com.romarjozeka.app.ws.shared.dto.UserDto;
import com.romarjozeka.app.ws.ui.model.response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private Utils utils;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private AmazonSES amazonSES;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, Utils utils, BCryptPasswordEncoder bCryptPasswordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, AmazonSES amazonSES) {
        this.userRepository = userRepository;
        this.utils = utils;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.amazonSES = amazonSES;
    }

    @Override
    public UserDto createUser(UserDto user) throws Exception {

        UserEntity userExists = userRepository.findByEmail(user.getEmail());

        if (userExists != null)
            throw new Exception("Account already exists!");

        for (int i = 0; i < user.getAddresses().size(); i++) {

            AddressDto address = user.getAddresses().get(i);
            address.setUserDetails(user);
            address.setAddressId(utils.generateAddressId(30));
        }

        ModelMapper modelMapper = new ModelMapper();

        UserEntity userEntity = modelMapper.map(user, UserEntity.class);

        String publicUserId = utils.generateUserId(30);
        userEntity.setUserId(publicUserId);

        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));

        UserEntity savedUser = userRepository.save(userEntity);


        UserDto returnNewUser = modelMapper.map(savedUser, UserDto.class);

        amazonSES.verifyEmail(returnNewUser);

        return returnNewUser;

    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmail(email);

        if (user == null) throw new UsernameNotFoundException(email);

        return new User(user.getEmail(), user.getEncryptedPassword(), user.getEmailVerificationStatus(), true, true, true, new ArrayList<>());
//        return new User(user.getEmail(), user.getEncryptedPassword(), new ArrayList<>());
    }

    @Override
    public UserDto getUser(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null)
            throw new UsernameNotFoundException(ErrorMessages.NO_SUCH_EMAIL_EXISTS.getErrorMessage());

        UserDto returnValue = new UserDto();
        BeanUtils.copyProperties(userEntity, returnValue);

        return returnValue;
    }

    @Override
    public UserDto getUserById(String userId) throws UsernameNotFoundException {

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null)
            throw new UsernameNotFoundException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        UserDto returnValue = new UserDto();

        BeanUtils.copyProperties(userEntity, returnValue);

        return returnValue;
    }

    @Override
    public UserDto updateUser(String userId, UserDto userDto) throws ResourceNotFoundException {

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) throw new ResourceNotFoundException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        userEntity.setFirstName(userDto.getFirstName());
        userEntity.setLastName(userDto.getLastName());

        UserEntity returnUpdatedUser = userRepository.save(userEntity);

        UserDto updatedUser = new UserDto();

        BeanUtils.copyProperties(returnUpdatedUser, updatedUser);

        return updatedUser;
    }

    @Override
    public void deleteUser(String userId) throws ResourceNotFoundException {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) throw new ResourceNotFoundException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        userRepository.delete(userEntity);
    }

    @Override
    public List<UserDto> getUsers(int page, int limit) {

        if (page > 0) page--;
        List<UserDto> returnValue = new ArrayList<>();

        Pageable pageableRequest = PageRequest.of(page, limit);

        Page<UserEntity> pageEntity = userRepository.findAll(pageableRequest);

        List<UserEntity> listEntity = pageEntity.getContent();

        listEntity.forEach(getUserEntity -> {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(getUserEntity, userDto);
            returnValue.add(userDto);
        });

        return returnValue;
    }

    @Override
    public boolean verifyEmailToken(String token) {
        boolean returnValue = false;

        UserEntity userEntity = userRepository.findByEmailVerificationToken(token);

        if (userEntity != null) {

            boolean hasTokenExpired = Utils.hasTokenExpired(token);

            if (!hasTokenExpired) {
                userEntity.setEmailVerificationToken(null);
                userEntity.setEmailVerificationStatus(true);
                userRepository.save(userEntity);
                returnValue = true;
            }
        }

        return returnValue;

    }

    public boolean requestPasswordReset(String email) {
        boolean returnValue = false;

        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity != null) {
            String token = utils.generatePasswordResetToken(userEntity.getUserId());
            PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
            passwordResetTokenEntity.setToken(token);
            passwordResetTokenEntity.setUserDetails(userEntity);
            passwordResetTokenRepository.save(passwordResetTokenEntity);

            returnValue = new AmazonSES().sendPasswordResetRequest(
                    userEntity.getFirstName(),
                    userEntity.getEmail(),
                    token);
        }

        return returnValue;
    }

    @Override
    public boolean resetPassword(String token, String password) {
        boolean returnValue = false;

        if (Utils.hasTokenExpired(token)) {
            return returnValue;
        }

        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);

        if (passwordResetTokenEntity == null) {
            return returnValue;
        }


        String encodedPassword = bCryptPasswordEncoder.encode(password);

        // Update User password in database
        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(encodedPassword);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        if (savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
            returnValue = true;
        }

        passwordResetTokenRepository.delete(passwordResetTokenEntity);

        return returnValue;
    }


}