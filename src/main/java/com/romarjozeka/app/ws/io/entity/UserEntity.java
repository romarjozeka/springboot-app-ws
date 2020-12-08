package com.romarjozeka.app.ws.io.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler","addresses"})
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 3185358572190076751L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private String userId;

    @NotNull
    @Size(min = 2, max = 100, message = "First name should be at least {min}.")
    private String firstName;

    @NotNull
    @Size(min = 2, max = 100, message = "Last name should be at least {min}.")
    private String lastName;

    @NotEmpty
    @Email(message = "Enter a valid email address.")
    private String email;

    @NotNull
    @Column(nullable = false)
    private String encryptedPassword;

    private String emailVerificationToken;

    @NotNull
    private boolean emailVerificationStatus = false;


    @OneToMany(mappedBy = "userDetails", cascade = CascadeType.ALL)
    private List<AddressEntity> addresses;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public boolean getEmailVerificationStatus() {
        return emailVerificationStatus;
    }

    public void setEmailVerificationStatus(boolean emailVerificationStatus) {
        this.emailVerificationStatus = emailVerificationStatus;
    }
    public boolean isEmailVerificationStatus() {
        return emailVerificationStatus;
    }

    public List<AddressEntity> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressEntity> addresses) {
        this.addresses = addresses;
    }
}
