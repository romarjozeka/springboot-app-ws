package com.romarjozeka.app.ws.service;

import com.romarjozeka.app.ws.shared.dto.AddressDto;

import java.util.List;

public interface AddressService {
    List<AddressDto> getAddresses(String userId);
}
