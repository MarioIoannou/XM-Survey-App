package com.marioioannou.surveyapp.utils

sealed class SubmissionStatus{
    object Success : SubmissionStatus()
    class Failure(val message: String) : SubmissionStatus()
    object None : SubmissionStatus()
}
