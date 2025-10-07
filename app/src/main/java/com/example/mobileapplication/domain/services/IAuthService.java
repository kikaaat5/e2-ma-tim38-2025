package com.example.mobileapplication.domain.services;

import com.example.mobileapplication.data.models.User;

public interface IAuthService {
    boolean register(User user, String password, String confirmPassword);
    boolean login(String email, String password);
    void logout();
    User getCurrentUser();
}
