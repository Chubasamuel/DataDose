package com.chubasamuel.datadose.di

import android.content.Context
import com.chubasamuel.datadose.data.local.AppDatabase
import com.chubasamuel.datadose.data.remote.GetUpdates
import com.chubasamuel.datadose.util.DCORPrefs
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext appContext: Context)=AppDatabase.getInstance(appContext)

    @Singleton
    @Provides
    fun provideAppDao(db:AppDatabase)= db.getAppDao()
    @Singleton
    @Provides
    fun providesDCORPrefs(@ApplicationContext appContext: Context) = DCORPrefs(appContext)
    @Singleton
    @Provides
    fun providesAppContext(@ApplicationContext appContext: Context) = appContext

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson) : Retrofit = Retrofit.Builder()
        .baseUrl("https://chubasamuel.github.io/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    fun provideGetUpdates(retrofit: Retrofit): GetUpdates = retrofit.create(GetUpdates::class.java)
}