package com.romarjozeka.app.ws.io.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="password_reset_tokens")
public class PasswordResetTokenEntity implements Serializable {

    private static final long serialVersionUID = -8916485204596724804L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String token;

    @OneToOne
    @JoinColumn(name="users_id")
    private UserEntity userDetails;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserEntity getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserEntity userDetails) {
        this.userDetails = userDetails;
    }
}
