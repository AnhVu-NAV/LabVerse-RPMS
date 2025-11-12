package com.prm392.g5.labverse.apiService;

import com.prm392.g5.labverse.dto.paper.PaperSummaryDTO;
import com.prm392.g5.labverse.dto.readingList.AddExistingPaperRequest;
import com.prm392.g5.labverse.dto.readingList.CreateReadingListRequest;
import com.prm392.g5.labverse.dto.readingList.ReadingListItemDTO;
import com.prm392.g5.labverse.dto.readingList.ReadingListResponse;
import com.prm392.g5.labverse.dto.readingList.ReadingListSummaryDTO;
import com.prm392.g5.labverse.dto.readingList.UpdateReadingListRequest;
import com.prm392.g5.labverse.entity.ReadingList;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReadingListApi {

    // CREATE
    @POST("/api/reading-lists")
    Call<ReadingListResponse> create(@Body CreateReadingListRequest body);

    // (nếu BE CHƯA có endpoint này, hãy xoá method này đi hoặc implement bên BE)
    @GET("/api/reading-lists")
    Call<List<ReadingListSummaryDTO>> listMine();

    // RENAME (BE đang trả entity ReadingList)
    @PUT("/api/reading-lists/{id}")
    Call<Void> rename(@Path("id") String id, @Body UpdateReadingListRequest body);

    // DELETE
    @DELETE("/api/reading-lists/{id}")
    Call<Void> delete(@Path("id") String id);

    // LIST PAPERS IN LIST -> PaperSummaryDTO (KHÔNG phải entity Paper)
    @GET("/api/reading-lists/{id}/papers")
    Call<List<PaperSummaryDTO>> getPapers(@Path("id") String id);

    // ADD EXISTING PAPER -> ReadingListItemDTO
    @POST("/api/reading-lists/{id}/papers")
    Call<ReadingListItemDTO> addExisting(@Path("id") String id, @Body AddExistingPaperRequest body);

    // XÓA 1 paper khỏi list
    @DELETE("/api/reading-lists/{id}/papers/{paperId}")
    Call<Void> deletePaper(@Path("id") String id, @Path("paperId") String paperId);
}
