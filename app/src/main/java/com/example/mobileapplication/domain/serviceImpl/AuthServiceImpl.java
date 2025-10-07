package com.example.mobileapplication.domain.serviceImpl;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.example.mobileapplication.data.models.User;
import com.example.mobileapplication.data.repository.UserRepository;
import com.example.mobileapplication.domain.services.IAuthService;

public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        Log.d("HILT_TEST", "✅ AuthServiceImpl uspešno injektovan!");
    }

    private String validateRegistrationInput(User user, String password, String confirmPassword) {
        if (user == null) return "Greška: Korisnički podaci nisu uneti.";
        if (TextUtils.isEmpty(user.getEmail())) return "Email ne sme biti prazan.";
        if (!Patterns.EMAIL_ADDRESS.matcher(user.getEmail()).matches())
            return "Email format nije ispravan.";
        if (TextUtils.isEmpty(password)) return "Lozinka ne sme biti prazna.";
        if (password.length() < 6) return "Lozinka mora imati najmanje 6 karaktera.";
        if (!password.equals(confirmPassword)) return "Lozinke se ne poklapaju.";
        if (TextUtils.isEmpty(user.getUsername())) return "Korisničko ime je obavezno.";
        if (user.getAvatarUrl() == null) return "Odaberi avatar.";
        return null;
    }

    @Override
    public boolean register(User user, String password, String confirmPassword) {
        String error = validateRegistrationInput(user, password, confirmPassword);
        if (error != null) {
            System.out.println("❌ Registracija neuspešna: " + error);
            return false;
        }
        return userRepository.registerUser(user, password);
    }

    @Override
    public boolean login(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            System.out.println("❌ Email i lozinka su obavezni.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            System.out.println("❌ Email format nije ispravan.");
            return false;
        }
        return userRepository.loginUser(email, password);
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
