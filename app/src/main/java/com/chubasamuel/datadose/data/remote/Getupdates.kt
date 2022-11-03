package com.chubasamuel.datadose.data.remote


import com.chubasamuel.datadose.data.models.APIModels
import retrofit2.Call
import retrofit2.http.GET


interface GetUpdates {
    @GET("/datadose/app_update.json")
    fun getAppUpdate(): Call<APIModels.AppUpdateAPIModel?>?

    @GET("/datadose/dev_update.json")
    fun getDevUpdate(): Call<APIModels.DevUpdateAPIModel?>?
}