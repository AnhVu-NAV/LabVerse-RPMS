package com.prm392.g5.labverse.dto.user;

public class UserSimpleResponse{

        String email;
        String roleName;

        public UserSimpleResponse(String email, String roleName) {
            this.email = email;
            this.roleName = roleName;
        }

        public String getEmail() {
            return email;
        }
        public String getRoleName() {
            return roleName;
        }

}