package com.chubasamuel.datadose.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.chubasamuel.datadose.data.local.AppDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(private val appDao: AppDao):ViewModel(){

}