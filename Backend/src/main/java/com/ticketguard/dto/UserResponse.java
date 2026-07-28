package com.ticketguard.dto;

public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String profileImage;

    // Constructors
    public UserResponse() {}

    public UserResponse(Long id, String firstName, String lastName, String email, String phone, String role, String status, String profileImage) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.profileImage = profileImage;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    // Builder
    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }

    public static class UserResponseBuilder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String role;
        private String status;
        private String profileImage;

        public UserResponseBuilder id(Long id) { this.id = id; return this; }
        public UserResponseBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public UserResponseBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public UserResponseBuilder email(String email) { this.email = email; return this; }
        public UserResponseBuilder phone(String phone) { this.phone = phone; return this; }
        public UserResponseBuilder role(String role) { this.role = role; return this; }
        public UserResponseBuilder status(String status) { this.status = status; return this; }
        public UserResponseBuilder profileImage(String profileImage) { this.profileImage = profileImage; return this; }

        public UserResponse build() {
            return new UserResponse(id, firstName, lastName, email, phone, role, status, profileImage);
        }
    }
}
