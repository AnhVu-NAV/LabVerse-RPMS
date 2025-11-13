package com.prm392.g5.labverse.dto.meta;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PaperMetaDto {
    @SerializedName("title")  public String title;      // "Attention Is All You Need"
    @SerializedName("authors") public List<AuthorDto> authors;
    @SerializedName("year")   public Integer year;      // 2017
    @SerializedName("venue")  public String venue;      // "NeurIPS" hoặc "Nature"
    @SerializedName("doi")    public String doi;        // "10.48550/arXiv.1706.03762"
}
