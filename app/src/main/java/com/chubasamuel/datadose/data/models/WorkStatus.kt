package com.chubasamuel.datadose.data.models

sealed interface WorkStatus{
    data class Waiting(val statusMessage: String?=null) : WorkStatus
    class Working(val statusMessage: String?=null) : WorkStatus
    class Finished(statusMessage: String?=null) : WorkStatus
    class ErrorOccurred(statusMessage: String?=null) : WorkStatus
}