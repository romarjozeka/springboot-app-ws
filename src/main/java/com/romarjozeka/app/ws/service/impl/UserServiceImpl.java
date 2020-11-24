package com.romarjozeka.app.ws.service.impl;

import com.romarjozeka.app.ws.exceptions.ResourceNotFoundException;
import com.romarjozeka.app.ws.io.entity.UserEntity;
import com.romarjozeka.app.ws.io.repository.UserRepository;
import com.romarjozeka.app.ws.service.UserService;
import com.romarjozeka.app.ws.shared.Utils;
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
    private Utils utils;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, Utils utils, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.utils = utils;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public UserDto createUser(UserDto user) throws Exception {

        UserEntity userExists = userRepository.findByEmail(user.getEmail());

        if (userExists != null) throw new Exception("Account already exists!");

        for (int i = 0; i < user.getAddresses().size(); i++) {

            AddressDto address = user.getAddresses().get(i);
            address.setUserDetails(user);
            System.out.println("UserDTO: "+address.getUserDetails());
            address.setAddressId(utils.generateAddressId(30));
        }

        ModelMapper modelMapper = new ModelMapper();

        UserEntity userEntity = modelMapper.map(user, UserEntity.class);

        String publicUserId = utils.generateUserId(30);
        userEntity.setUserId(publicUserId);

        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        UserEntity savedUser = userRepository.save(userEntity);

        UserDto returnNewUser = modelMapper.map(savedUser, UserDto.class);

        return returnNewUser;

    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmail(email);

        if (user == null) throw new UsernameNotFoundException(email);
        return new User(user.getEmail(), user.getEncryptedPassword(), new ArrayList<>());
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
}