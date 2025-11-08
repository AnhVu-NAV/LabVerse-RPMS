package com.prm392.g5.labverse.repository;

import com.prm392.g5.labverse.apiService.PaperAnnotationApiService;
import com.prm392.g5.labverse.apiService.ReadingStatusApiService;
import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.readingStatus.ReadingStatusRequest;
import com.prm392.g5.labverse.dto.readingStatus.ReadingStatusResponse;

import retrofit2.Callback;

public class ReadingStatusRepository {
    private ReadingStatusApiService readingStatusApiService;

    public ReadingStatusRepository() {
        this.readingStatusApiService = RetrofitClient.getInstance().create(ReadingStatusApiService.class);
    }

    public void getReadingStatusInfo(String userId, String paperId, Callback<ReadingStatusResponse> callback){
        readingStatusApiService.getReadingStatusInfo(userId, paperId).enqueue(callback);
    }

    public void createOrUpdateReadingStatus(ReadingStatusRequest request, Callback<ReadingStatusResponse> callback) {
        readingStatusApiService.createOrUpdateReadingStatus(request).enqueue(callback);
    }

}
