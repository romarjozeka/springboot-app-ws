package com.romarjozeka.app.ws.service.impl;

import com.romarjozeka.app.ws.io.entity.AddressEntity;
import com.romarjozeka.app.ws.io.entity.UserEntity;
import com.romarjozeka.app.ws.io.repository.PasswordResetTokenRepository;
import com.romarjozeka.app.ws.io.repository.UserRepository;
import com.romarjozeka.app.ws.shared.Utils;
import com.romarjozeka.app.ws.shared.aws.AmazonSES;
import com.romarjozeka.app.ws.shared.dto.AddressDto;
import com.romarjozeka.app.ws.shared.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    Utils utils;

    @Mock
    AmazonSES amazonSES;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    String userId = "hhty57ehfy";

    String encryptedPassword = "zjgy55FGFE5sf";

    UserEntity userEntity;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFirstName("Romarjo");
        userEntity.setLastName("Zeka");
        userEntity.setUserId(userId);
        userEntity.setEncryptedPassword(encryptedPassword);
        userEntity.setEmail("romarjozeka@gmail.com");
        userEntity.setEmailVerificationToken("eyJtJhsf47j45");
        userEntity.setAddresses(getAddressesEntity());
    }

    @Test
    final void testGetUser() {

        when(userRepository.findByEmail(anyString())).thenReturn(userEntity);

        UserDto userDto = userService.getUser("romarjozeka@gmail.com");

        assertNotNull(userDto);
        assertEquals("Romarjo", userDto.getFirstName());

    }

    @Test
    final void testGetUser_UsernameNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,

                () -> {
                    userService.getUser("romarjozeka@gmail.com");
                }

        );
    }

    @Test
    final void testCreateUser_CreateUserServiceException() {
        when(userRepository.findByEmail(anyString())).thenReturn(userEntity);
        UserDto userDto = new UserDto();
        userDto.setAddresses(getAddressesDto());
        userDto.setFirstName("Romarjo");
        userDto.setLastName("Zeka");
        userDto.setPassword("verygoodpassword");
        userDto.setEmail("romarjozeka@gmail.com");

        assertThrows(Exception.class,

                () -> {
                    userService.createUser(userDto);
                }

        );
    }

    @Test
    final void testCreateUser() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(utils.generateAddressId(anyInt())).thenReturn("JhsfU3THDshsh");
        when(utils.generateUserId(anyInt())).thenReturn(userId);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(encryptedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        Mockito.doNothing().when(amazonSES).verifyEmail(any(UserDto.class));

        UserDto userDto = new UserDto();
        userDto.setAddresses(getAddressesDto());
        userDto.setFirstName("Romarjo");
        userDto.setLastName("Zeka");
        userDto.setPassword("verygoodpassword");
        userDto.setEmail("romarjozeka@gmail.com");

        UserDto storedUserDetails = userService.createUser(userDto);
        assertNotNull(storedUserDetails);
        assertEquals(userEntity.getFirstName(), storedUserDetails.getFirstName());
        assertEquals(userEntity.getLastName(), storedUserDetails.getLastName());
        assertNotNull(storedUserDetails.getUserId());
        assertEquals(storedUserDetails.getAddresses().size(), userEntity.getAddresses().size());
        verify(utils, times(storedUserDetails.getAddresses().size())).generateAddressId(30);
        verify(bCryptPasswordEncoder, times(1)).encode("verygoodpassword");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    private List<AddressDto> getAddressesDto() {
        AddressDto addressDto = new AddressDto();
        addressDto.setType("shipping");
        addressDto.setCity("Vancouver");
        addressDto.setCountry("Canada");
        addressDto.setPostcode("ABC123");
        addressDto.setAddressName("123 Street name");

        AddressDto billingAddressDto = new AddressDto();
        billingAddressDto.setType("billling");
        billingAddressDto.setCity("Vancouver");
        billingAddressDto.setCountry("Canada");
        billingAddressDto.setPostcode("ABC123");
        billingAddressDto.setAddressName("123 Street name");

        List<AddressDto> addresses = new ArrayList<>();
        addresses.add(addressDto);
        addresses.add(billingAddressDto);

        return addresses;

    }

    private List<AddressEntity> getAddressesEntity() {
        List<AddressDto> addresses = getAddressesDto();

        Type listType = new TypeToken<List<AddressEntity>>() {
        }.getType();

        return new ModelMapper().map(addresses, listType);
    }

}