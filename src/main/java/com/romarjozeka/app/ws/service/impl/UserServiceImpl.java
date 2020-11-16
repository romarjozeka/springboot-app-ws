package com.romarjozeka.app.ws.service.impl;

import com.romarjozeka.app.ws.io.entity.UserEntity;
import com.romarjozeka.app.ws.io.repository.UserRepository;
import com.romarjozeka.app.ws.service.UserService;
import com.romarjozeka.app.ws.shared.Utils;
import com.romarjozeka.app.ws.shared.dto.UserDto;
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

        if (userExists != null) throw new RuntimeException("Record already exists");
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
    public UserDto getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null)
            throw new UsernameNotFoundException(email);

        UserDto returnValue = new UserDto();
        BeanUtils.copyProperties(userEntity, returnValue);

        return returnValue;
    }

    @Override
    public UserDto getUserById(String userId) {

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null)
            throw new UsernameNotFoundException(userId);

        UserDto returnValue = new UserDto();

        BeanUtils.copyProperties(userEntity, returnValue);

        return returnValue;
    }
}