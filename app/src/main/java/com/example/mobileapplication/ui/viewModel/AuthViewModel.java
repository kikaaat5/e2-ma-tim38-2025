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
        boolean result = authService.login(email, password);
        if (result) {
            loginSuccess.postValue(true);
        } else {
            errorMessage.postValue("Neuspešna prijava. Proveri podatke.");
            loginSuccess.postValue(false);
        }
    }

    public void register(User user, String password, String confirmPassword) {
        boolean result = authService.register(user, password, confirmPassword);
        if (result) {
            registrationSuccess.postValue(true);
        } else {
            errorMessage.postValue("Registracija neuspešna. Proveri unete podatke.");
            registrationSuccess.postValue(false);
        }
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
