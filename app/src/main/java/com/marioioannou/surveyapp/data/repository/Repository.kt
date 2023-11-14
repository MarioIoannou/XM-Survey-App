package com.marioioannou.surveyapp.data.repository

import com.marioioannou.surveyapp.data.model.Answer
import com.marioioannou.surveyapp.data.model.Question
import com.marioioannou.surveyapp.data.remote.QuestionsApi
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Response
import javax.inject.Inject

@ActivityRetainedScoped
class Repository @Inject constructor(
    private val questionsApi: QuestionsApi
) {
    suspend fun getQuestions() : Response<List<Question>> {
        return questionsApi.getQuestions()
    }

    suspend fun submitAnswer(questionId: Int, answerText: String): Response<Void> {
        val answer = Answer(questionId, answerText)
        return questionsApi.submitAnswer(answer)
    }

}