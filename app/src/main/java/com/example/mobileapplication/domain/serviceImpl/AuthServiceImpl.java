package com.example.mobileapplication.domain.serviceImpl;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.example.mobileapplication.data.models.User;
import com.example.mobileapplication.data.repository.UserRepository;
import com.example.mobileapplication.domain.services.IAuthService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;

public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        Log.d("HILT_TEST", "✅ AuthServiceImpl uspešno injektovan!");
    }

    private String validate(User user, String password, String confirmPassword) {
        if (user == null) return "Greška: Korisnički podaci nisu uneti.";
        if (TextUtils.isEmpty(user.getEmail())) return "Email ne sme biti prazan.";
        if (!Patterns.EMAIL_ADDRESS.matcher(user.getEmail()).matches()) return "Email format nije ispravan.";
        if (TextUtils.isEmpty(password)) return "Lozinka ne sme biti prazna.";
        if (password.length() < 6) return "Lozinka mora imati najmanje 6 karaktera.";
        if (!password.equals(confirmPassword)) return "Lozinke se ne poklapaju.";
        if (TextUtils.isEmpty(user.getUsername())) return "Korisničko ime je obavezno.";
        return null;
    }

    public void register(User user, String password, String confirmPassword, OnCompleteListener<AuthResult> listener) {
        String error = validate(user, password, confirmPassword);
        if (error != null) {
            Log.e("AuthService", error);
            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(new Exception(error)));
            return;
        }
        userRepository.registerUser(user, password, listener);
    }

    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(new Exception("Email i lozinka su obavezni.")));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(new Exception("Neispravan format email adrese.")));
            return;
        }
        userRepository.loginUser(email, password, listener);
    }

    @Override
    public void logout() {
        userRepository.logoutUser();
    }

    @Override
    public User getCurrentUser() {
        return userRepository.getCurrentUser();
    }
}
