package com.prm392.g5.labverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.prm392.g5.labverse.entity.UserEntity;
import com.prm392.g5.labverse.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final LiveData<UserEntity> userProfile;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        userProfile = repository.getUserProfile();
    }

    public LiveData<UserEntity> getUserProfile() {
        return userProfile;
    }

    public void refreshUser(String id) {
        repository.refreshUser(id);
    }

    public void updateUser(UserEntity user) {
        repository.updateUserOnline(user);
    }
}
