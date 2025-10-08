package com.example.mobileapplication.data.repository;

import com.example.mobileapplication.data.models.User;

public class UserRepository {

    public UserRepository(){

    }

    public boolean registerUser(User user, String password) {
        // TODO: implementirati FirebaseAuth.createUserWithEmailAndPassword()
        // i snimanje korisnika u bazu
        return true;
    }

    public boolean loginUser(String email, String password) {
        // TODO: implementirati FirebaseAuth.signInWithEmailAndPassword()

        return true;
    }

    public User getCurrentUser() {
        // TODO: povezati sa Firebase-om
        return null;
    }

    public void logoutUser() {
        // TODO: implementirati FirebaseAuth.signOut()
    }
}
