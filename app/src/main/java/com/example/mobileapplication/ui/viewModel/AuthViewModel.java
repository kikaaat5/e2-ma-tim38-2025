package com.example.mobileapplication.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobileapplication.data.models.User;
import com.example.mobileapplication.data.repository.UserRepository;
import com.example.mobileapplication.domain.serviceImpl.AuthServiceImpl;
import com.example.mobileapplication.domain.services.IAuthService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final IAuthService authService;
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    @Inject
    public AuthViewModel(IAuthService authService) {

        this.authService = authService;
    }

    public void login(String email, String password) {
        authService.login(email, password, task -> {
            if (task.isSuccessful()) {
                loginSuccess.postValue(true);
            } else {
                Exception e = task.getException();
                errorMessage.postValue(
                        e != null ? "❌ Login neuspešan: " + e.getMessage() : "❌ Login neuspešan."
                );
                loginSuccess.postValue(false);
            }
        });
    }


    public void register(User user, String password, String confirmPassword) {
        authService.register(user, password, confirmPassword, task -> {
            if (task.isSuccessful()) {
                registrationSuccess.postValue(true);
            } else {
                Exception e = task.getException();
                errorMessage.postValue(
                        e != null ? " Registracija neuspešna: " + e.getMessage() : "Registracija neuspešna."
                );
                registrationSuccess.postValue(false);
            }
        });
    }

    public void logout() {
        authService.logout();
    }

    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<Boolean> getRegistrationSuccess() { return registrationSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
