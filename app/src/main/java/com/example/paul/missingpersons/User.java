package com.example.paul.missingpersons;

/**
 * Created by paul on 30/10/2017.
 */

public class User {
    private int id;
    private String username, email,token;
    private Long expiry;

    public User(int id, String username, String email, String token, Long expiry) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.token = token;
        this.expiry = expiry;
    }

    public int getId() {
        return id;
    }

    public String getToken() {
        return token;
    }


    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Long getExpiry() {
        return expiry;
    }


}
