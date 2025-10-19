package com.example.mobileapplication.domain.services;

import com.example.mobileapplication.data.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;

public interface IAuthService {
    void register(User user, String password, String confirmPassword, OnCompleteListener<AuthResult> listener);
    void login(String email, String password, OnCompleteListener<AuthResult> listener) ;
    void logout();
    User getCurrentUser();
}
