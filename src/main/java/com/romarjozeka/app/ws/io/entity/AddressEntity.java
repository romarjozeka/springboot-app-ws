package com.romarjozeka.app.ws.io.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "addresses")
public class AddressEntity implements Serializable {

    private static final long serialVersionUID = -4760911309589816481L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @Size(min = 30, message = "Address ID should be at least {min}.")
    private String addressId;


    @NotNull
    @Size(max = 10, message = "Type should be at most {max}.")
    private String type;

    @NotNull
    @Size(max = 100, message = "Address should be at most {max}.")
    private String addressName;

    @NotNull
    @Size(max = 10, message = "Postcode should be at most {max}.")
    private String postcode;

    @NotNull
    @Size(max = 15, message = "Country name should be at most {max}.")
    private String country;

    @NotNull
    @Size(max = 15, message = "City name should be at most {max}.")
    private String city;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private UserEntity userDetails;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public UserEntity getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserEntity userDetails) {
        this.userDetails = userDetails;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}