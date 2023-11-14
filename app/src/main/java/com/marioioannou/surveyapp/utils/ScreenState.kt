package com.marioioannou.surveyapp.utils

sealed class ScreenState<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T?): ScreenState<T>(data)
    class Loading<T>: ScreenState<T>()
    class Error<T>(data: T?= null, message: String?): ScreenState<T>(data,message)
}