package com.romarjozeka.app.ws.security;

import com.romarjozeka.app.ws.SpringApplicationContext;

public class SecurityConstants {
    public static final long EXPIRATION_TIME = 86400000; // 24 hours in ms
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/users";
    public static final String TOKEN_STRING = "jf9i4jgu83nfl0";
    public static final String EMAIL_VERIFICATION_URL="/users/email-verification";
    public static String getTokenSecret() {

        AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("AppProperties");

        return appProperties.getTokenSecret();
    }
}
