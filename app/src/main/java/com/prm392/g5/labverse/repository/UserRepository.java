package com.prm392.g5.labverse.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.prm392.g5.labverse.apiService.UserApiService;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dao.UserDao;
import com.prm392.g5.labverse.dto.user.RegisterAccountRequest;
import com.prm392.g5.labverse.dto.user.UpdateUserRequest;
import com.prm392.g5.labverse.dto.user.UserDto;
import com.prm392.g5.labverse.dto.user.UserSimpleResponse;
import com.prm392.g5.labverse.entity.UserEntity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final UserApiService userApiService;
    private UserDao userDao;
    private LiveData<UserEntity> userLive;


    public UserRepository() {
        userApiService = RetrofitClient.getInstance().create(UserApiService.class);
    }

    public void registerAccount(RegisterAccountRequest request, Callback<UserSimpleResponse> callback) {
        userApiService.registerAccount(request).enqueue(callback);
    }

    public void resendOtpVerifyAccount(String email, Callback<ResponseBody> callback) {
        userApiService.resendOtpVerifyAccount(email).enqueue(callback);
    }

    public void verifyAccount(String email, String otp, Callback<ResponseBody> callback) {
        userApiService.verifyAccount(email, otp).enqueue(callback);
    }

    public void selectRole(String userId, String roleName, Callback<UserSimpleResponse> callback) {
        userApiService.selectRole(userId, roleName).enqueue(callback);
    }

    public UserRepository(Application app) {
        userDao = AppDatabase.getInstance(app).userDao();
        userLive = userDao.getUserProfile();
        userApiService = RetrofitClient.getInstance().create(UserApiService.class);
    }

    public LiveData<UserEntity> getUserProfile() { return userLive; }

    // Refresh từ server (nếu bạn có /api/users/{id} thì đổi cho phù hợp, mặc định /me)
    public void refreshUser(String id) {
        userApiService.getMe().enqueue(new Callback<UserDto>() {
            @Override public void onResponse(
                    Call<UserDto> call,
                    Response<UserDto> res) {
                if (res.isSuccessful() && res.body()!=null) {
                    AppDatabase.databaseWriteExecutor.execute(() ->
                            userDao.insertOrUpdate(map(res.body())));
                }
            }
            @Override public void onFailure(Call<UserDto> call, Throwable t) {
                Log.w("UserRepo","refreshUser fail: "+t.getMessage());
            }
        });
    }

    /** Optimistic update: ghi local trước, gọi API sau */
    public void updateUserOnline(UserEntity updated) {
        // 1) upsert local với cờ pending
        AppDatabase.databaseWriteExecutor.execute(() -> {
            updated.setPendingUpdate(true);
            userDao.insertOrUpdate(updated);
        });

        // 2) gọi API
        UpdateUserRequest req = new UpdateUserRequest();
        req.full_name     = updated.getFullName();
        req.phone_number  = updated.getPhoneNumber();
        req.gender        = updated.getGender();   // Boolean
        req.address       = updated.getAddress();

        userApiService.updateMe(req).enqueue(new Callback<UserDto>() {
            @Override public void onResponse(Call<UserDto> call, Response<UserDto> res) {
                if (res.isSuccessful() && res.body() != null) {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        UserEntity fresh = map(res.body());
                        fresh.setPendingUpdate(false);
                        userDao.insertOrUpdate(fresh);
                    });
                } // else: giữ pending_update = true để đồng bộ lại sau
            }
            @Override public void onFailure(Call<UserDto> call, Throwable t) {
                // offline/lỗi mạng -> pending_update giữ nguyên
            }
        });
    }

    private static UserEntity map(UserDto d) {
        UserEntity u = new UserEntity();
        u.setId(d.id != null ? d.id : SharePreferenceManager.getInstance().getUserId());
        u.setFullName(d.full_name);
        u.setEmail(d.email);
        u.setPhoneNumber(d.phone_number);
        u.setGender(d.gender);
        u.setAddress(d.address);
        u.setRoleId(d.role_id != null ? d.role_id : 3);
        u.setDeleteFlag(d.delete_flag != null ? d.delete_flag : false);
        u.setCreatedAt(d.created_at);
        u.setUpdatedAt(d.updated_at);
        return u;
    }

}
