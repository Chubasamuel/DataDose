package com.chubasamuel.datadose.di

import android.content.Context
import com.chubasamuel.datadose.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

}