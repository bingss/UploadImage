package com.example.uploadimagetest.data

import com.google.gson.annotations.SerializedName

data class CaseData(
    @SerializedName("MT01")
    val year: String = "",
    @SerializedName("MT02")
    val unid: String= "",
    @SerializedName("MT03")
    val caseid: String= "",
    @SerializedName("MT04")
    val sectno: String= "",
    @SerializedName("MT05")
    val order: Int,
    @SerializedName("MT06")
    val landno: String= "",
    val MT07: Any? = null,
    val MT08: String= "",
    val MT09: Double = 0.0
)
