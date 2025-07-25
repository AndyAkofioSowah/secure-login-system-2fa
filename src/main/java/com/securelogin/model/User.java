package com.securelogin.model;

import jakarta.persistence.*;

@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String totpSecret;

    // --- GETTERS & SETTERS ------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getTotpSecret() {
        return totpSecret;
    }
    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }
}
