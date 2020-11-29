package com.romarjozeka.app.ws.ui.controller;

import com.romarjozeka.app.ws.exceptions.ResourceNotFoundException;
import com.romarjozeka.app.ws.service.impl.AddressServiceImpl;
import com.romarjozeka.app.ws.service.impl.UserServiceImpl;
import com.romarjozeka.app.ws.shared.dto.AddressDto;
import com.romarjozeka.app.ws.shared.dto.UserDto;
import com.romarjozeka.app.ws.ui.model.request.PasswordResetModel;
import com.romarjozeka.app.ws.ui.model.request.PasswordResetRequestModel;
import com.romarjozeka.app.ws.ui.model.request.UserDetailsRequestModel;
import com.romarjozeka.app.ws.ui.model.response.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {


    private UserServiceImpl userService;
    private AddressServiceImpl addressService;

    @Autowired
    public UserController(UserServiceImpl userService, AddressServiceImpl addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }

    /**
     * Users
     */

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<UserRest> getAllUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "limit", defaultValue = "25") int limit
    ) {

        List<UserRest> returnValue = new ArrayList<>();


        List<UserDto> list = userService.getUsers(page, limit);


        list.forEach(getUserDto -> {
            UserRest userRest = new UserRest();
            BeanUtils.copyProperties(getUserDto, userRest);
            returnValue.add(userRest);
        });
        return returnValue;
    }

    @GetMapping(path = "/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest getUser(@PathVariable String userId) {
        UserRest returnValue = new UserRest();

        UserDto user = userService.getUserById(userId);

        BeanUtils.copyProperties(user, returnValue);

        return returnValue;
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest createUser(@Valid @RequestBody UserDetailsRequestModel userDetails) throws Exception {

        ModelMapper modelMapper = new ModelMapper();

        UserDto userDto = modelMapper.map(userDetails, UserDto.class);


        UserDto createdUser = userService.createUser(userDto);

        UserRest returnValue = modelMapper.map(createdUser, UserRest.class);

        return returnValue;
    }

    @PutMapping(path = "/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest updateUser(@PathVariable String userId, @Valid @RequestBody UserDetailsRequestModel userDetails) throws Exception {

        UserRest returnValue = new UserRest();

        UserDto userDto = new UserDto();

        BeanUtils.copyProperties(userDetails, userDto);

        UserDto updatedUser = userService.updateUser(userId, userDto);

        BeanUtils.copyProperties(updatedUser, returnValue);

        return returnValue;
    }

    @DeleteMapping(path = "/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel deleteUser(@PathVariable String userId) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        userService.deleteUser(userId);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        return returnValue;
    }


    /**
     * Addresses
     */

    @GetMapping(path = "/{userId}/addresses", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public CollectionModel<AddressRest> getAddresses(@PathVariable String userId) {

        List<AddressDto> list = addressService.getAddresses(userId);

        if (list == null && list.isEmpty()) throw new ResourceNotFoundException("No addresses found!");

        Type listType = new TypeToken<List<AddressRest>>() {
        }.getType();

        List<AddressRest> returnValue = new ModelMapper().map(list, listType);

        returnValue.stream().forEach(address -> {

            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddress(userId, address.getAddressId())).withSelfRel();
            address.add(selfLink);
        });

        Link userLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUser(userId)).withRel("user");

        Link addressLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddresses(userId)).withRel("addresses");

        return CollectionModel.of(returnValue, Arrays.asList(userLink, addressLink));
    }

    @GetMapping(path = "/{userId}/addresses/{addressId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public EntityModel<AddressRest> getAddress(@PathVariable String userId, @PathVariable String addressId) {

        AddressDto addressDto = addressService.getAddressById(addressId);

        if (addressDto == null) throw new ResourceNotFoundException("Tha address was not found!");

        ModelMapper modelMapper = new ModelMapper();

        AddressRest returnValue = modelMapper.map(addressDto, AddressRest.class);

        Link userLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUser(userId)).withRel("user");

        Link addressLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddresses(userId)).withRel("addresses");

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddress(userId, addressId)).withSelfRel();

        return EntityModel.of(returnValue, Arrays.asList(userLink, addressLink, selfLink));
    }

    @GetMapping(path = "/email-verification", produces = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {

        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());

        boolean isVerified = userService.verifyEmailToken(token);

        if (isVerified) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        } else {
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        }

        return returnValue;
    }

    @PostMapping(path = "/password-reset-request",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {
        OperationStatusModel returnValue = new OperationStatusModel();

        boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());

        returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

        if (operationResult) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }

        return returnValue;
    }

    @PostMapping(path = "/password-reset",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel) {
        OperationStatusModel returnValue = new OperationStatusModel();

        boolean operationResult = userService.resetPassword(
                passwordResetModel.getToken(),
                passwordResetModel.getPassword());

        returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

        if (operationResult) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }

        return returnValue;
    }

}
