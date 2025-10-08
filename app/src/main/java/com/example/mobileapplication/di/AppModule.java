package com.example.mobileapplication.di;

import com.example.mobileapplication.data.repository.UserRepository;
import com.example.mobileapplication.domain.serviceImpl.AuthServiceImpl;
import com.example.mobileapplication.domain.services.IAuthService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public UserRepository provideUserRepository() {
        return new UserRepository();
    }

    @Provides
    @Singleton
    public IAuthService provideAuthService(UserRepository repository) {
        return new AuthServiceImpl(repository);
    }
}