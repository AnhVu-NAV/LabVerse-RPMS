package com.prm392.g5.labverse.dto.meta;

import com.google.gson.annotations.SerializedName;

public class AuthorDto {
    @SerializedName("fullName") public String fullName; // "Andrew Ng"
    @SerializedName("given")    public String given;    // "Andrew"
    @SerializedName("family")   public String family;   // "Ng"
}
