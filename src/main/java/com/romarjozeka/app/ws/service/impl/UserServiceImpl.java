package com.romarjozeka.app.ws.service.impl;

import com.romarjozeka.app.ws.exceptions.ResourceNotFoundException;
import com.romarjozeka.app.ws.io.entity.UserEntity;
import com.romarjozeka.app.ws.io.repository.UserRepository;
import com.romarjozeka.app.ws.service.UserService;
import com.romarjozeka.app.ws.shared.Utils;
import com.romarjozeka.app.ws.shared.dto.UserDto;
import com.romarjozeka.app.ws.ui.model.response.ErrorMessages;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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
    public UserDto createUser(UserDto user) {

        UserEntity userExists = userRepository.findByEmail(user.getEmail());

        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(user, userEntity);

        String publicUserId = utils.generateUserId(30);
        userEntity.setUserId(publicUserId);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        UserEntity savedUser = userRepository.save(userEntity);

        UserDto returnNewUser = new UserDto();
        BeanUtils.copyProperties(savedUser, returnNewUser);

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
    public void deleteUser(String userId) throws ResourceNotFoundException{
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) throw new ResourceNotFoundException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        userRepository.delete(userEntity);
    }
}