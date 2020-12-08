package com.romarjozeka.app.ws.ui.controller;

import com.romarjozeka.app.ws.ui.model.request.LoginRequestModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication API specification for Swagger documentation and Code Generation.
 * Implemented by Spring Security.
 */
@RestController
public class AuthenticationController {

    /**
     * Implemented by Spring Security
     */
    @ApiOperation("User login")
    @PostMapping("/users/login")
    public void theFakeLogin(@RequestBody LoginRequestModel loginRequestModel) {
        throw new IllegalStateException("This method should not be called. This method is implemented by Spring Security");
    }
}
