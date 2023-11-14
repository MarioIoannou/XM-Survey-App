package com.marioioannou.surveyapp.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marioioannou.surveyapp.data.model.Question
import com.marioioannou.surveyapp.data.repository.Repository
import com.marioioannou.surveyapp.utils.ScreenState
import com.marioioannou.surveyapp.utils.SubmissionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val repository: Repository,
    application: Application,
) : AndroidViewModel(application) {

    private var failedQuestionId: Int? = null
    private var failedAnswer: String? = null


    private val _surveyQuestions =
        MutableStateFlow<ScreenState<List<Question>>>(ScreenState.Loading())
    val surveyQuestions: StateFlow<ScreenState<List<Question>>> = _surveyQuestions.asStateFlow()

    private val _submittedAnswers = MutableStateFlow(0)
    val submittedAnswers: StateFlow<Int> = _submittedAnswers.asStateFlow()

    private val _isSubmitEnabled = MutableStateFlow(false)
    val isSubmitEnabled: StateFlow<Boolean> = _isSubmitEnabled.asStateFlow()

    private var _currentQuestionId = MutableStateFlow<Int?>(null)
    val currentQuestionId: StateFlow<Int?> = _currentQuestionId.asStateFlow()

    private val _submissionStatus = MutableStateFlow<SubmissionStatus>(SubmissionStatus.None)
    val submissionStatus: StateFlow<SubmissionStatus> = _submissionStatus.asStateFlow()


    fun submitAnswer(questionId: Int, answer: String) {
        viewModelScope.launch {
            try {
                val response = repository.submitAnswer(questionId, answer)
                if (response.isSuccessful) {
                    _submittedAnswers.value++
                    _surveyQuestions.value = updateQuestionAsSubmitted(questionId, answer)
                    _submissionStatus.value = SubmissionStatus.Success
                    failedQuestionId = null
                    failedAnswer = null
                } else {
                    _submissionStatus.value = SubmissionStatus.Failure("Failed")
                    failedQuestionId = questionId
                    failedAnswer = answer
                }
            } catch (e: IOException) {
                _submissionStatus.value = SubmissionStatus.Failure("Network Error")
                failedQuestionId = questionId
                failedAnswer = answer
            }
        }
    }

    private fun updateQuestionAsSubmitted(
        questionId: Int,
        answer: String,
    ): ScreenState<List<Question>> {
        val currentQuestions = _surveyQuestions.value.data ?: return ScreenState.Loading()
        val updatedQuestions = mutableListOf<Question>()
        for (question in currentQuestions) {
            if (question.id == questionId) {
                val updatedQuestion = question.copy(answer = answer, isSubmitted = true)
                updatedQuestions.add(updatedQuestion)
                checkSubmittedAnswer(updatedQuestion)
            } else {
                updatedQuestions.add(question)
            }
        }
        return ScreenState.Success(updatedQuestions)
    }

    fun checkSubmittedAnswer(currentQuestion: Question) {
        _isSubmitEnabled.value = currentQuestion.answer.isNotBlank() && !currentQuestion.isSubmitted
    }

    fun resetSurvey() {
        _submittedAnswers.value = 0
    }

    fun retrySubmission() {
        val questionId = failedQuestionId
        val answer = failedAnswer
        if (questionId != null && answer != null) {
            submitAnswer(questionId, answer)
        }
    }

    fun clearSubmissionStatus() {
        _submissionStatus.value = SubmissionStatus.None
    }

    fun getQuestions() = viewModelScope.launch {
        getQuestionsConnected()
    }


    private suspend fun getQuestionsConnected() {
        _surveyQuestions.value = ScreenState.Loading()
        try {
            if (hasInternetConnection()) {
                val response = repository.getQuestions()
                _surveyQuestions.value = handleQuestionsResponse(response)
            } else {
                _surveyQuestions.value = ScreenState.Error(null, "No internet connection")
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> _surveyQuestions.value =
                    ScreenState.Error(null, "Network Failure")

                else -> _surveyQuestions.value = ScreenState.Error(null, "Something went wrong")
            }
        }
    }

    private fun handleQuestionsResponse(response: Response<List<Question>>): ScreenState<List<Question>> {
        return when {
            response.isSuccessful -> {
                val questionsResponse = response.body()
                ScreenState.Success(questionsResponse)
            }
            response.body() == null -> {
                ScreenState.Error(null, "Data not found")
            }
            response.code() == 400 -> {
                ScreenState.Error(null, "Unsuccessful")
            }
            else -> {
                ScreenState.Error(null, "An error occurred while fetching the questions.")
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<Application>().getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun setCurrentQuestion(question: Question) {
        _currentQuestionId.value = question.id
    }

}
