package com.marioioannou.surveyapp.data.remote

import com.marioioannou.surveyapp.data.model.Answer
import com.marioioannou.surveyapp.data.model.Question
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface QuestionsApi {

    @GET("/questions")
    suspend fun getQuestions(): Response<List<Question>>

    @POST("/question/submit")
    suspend fun submitAnswer(@Body answer: Answer): Response<Void>
}