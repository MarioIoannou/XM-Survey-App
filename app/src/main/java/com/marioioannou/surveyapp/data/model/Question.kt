package com.marioioannou.surveyapp.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Question(
    @SerializedName("id")
    val id: Int,
    @SerializedName("question")
    val question: String,
    var answer: String,
    var isSubmitted: Boolean = false
) : Parcelable