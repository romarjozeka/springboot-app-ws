package com.romarjozeka.app.ws.service;

import com.romarjozeka.app.ws.shared.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    UserDto createUser(UserDto userDto);

    UserDto getUser(String email);

    UserDto getUserById(String userId);

    UserDto updateUser(String userId, UserDto userDto);

    void deleteUser(String userId);

    List<UserDto> getUsers(int page, int limit);
}
