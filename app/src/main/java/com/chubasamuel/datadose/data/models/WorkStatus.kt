package com.chubasamuel.datadose.data.models

sealed interface WorkStatus{
    class Waiting(statusMessage: String?=null) : WorkStatus
    class Working(statusMessage: String?=null) : WorkStatus
    class Finished(statusMessage: String?=null) : WorkStatus
    class ErrorOccurred(statusMessage: String?=null) : WorkStatus
}