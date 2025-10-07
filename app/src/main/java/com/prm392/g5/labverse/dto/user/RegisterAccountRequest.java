package com.prm392.g5.labverse.dto.user;

public class RegisterAccountRequest {

    private String email;

    //The password must have at least 8 characters, contain at least 1 digit
    private String password;

    private String roleName;
}
