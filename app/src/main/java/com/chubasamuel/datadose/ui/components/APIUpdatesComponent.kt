package com.chubasamuel.datadose.ui.components

import android.util.Log
import androidx.compose.material.AlertDialog
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogProperties
import com.chubasamuel.datadose.data.models.APIModels.APIUpdates

@Composable
fun AppUpdateComponent(update:APIUpdates?,resetVal:()->Unit,saveLast:()->Unit,launchPlayStore:()->Unit,showSnackBar:(String)->Unit){
    val showAlert by derivedStateOf { update!=null }
    if(showAlert) {
        when(update) {
            is APIUpdates.ForceFulAlert->{
            showAPIAlert(
                title = { Text(update.title) },
                text = { Text(update.message) },
                onConfirm = {
                    TextButton(onClick = { launchPlayStore() }) {
                        Text("Update")
                    }
                },
            )}
        is APIUpdates.NormalAlert-> {
            showAPIAlert(title = { Text(update.title) },
                text = { Text(update.message) },
                onConfirm = {
                    TextButton(onClick = {
                        saveLast(); resetVal(); launchPlayStore()
                    }) {
                        Text("Update")
                    }
                },
                onDismiss = {
                    TextButton(onClick = { saveLast(); resetVal() }) {
                        Text("Cancel")
                    }
                }
            )}
        is APIUpdates.SnackBar-> {
           showSnackBar(update.message)
        }
            else -> {}
        }
    }}

@Composable
fun DevUpdateComponent(update:APIUpdates?,resetVal: () -> Unit,saveLast:()->Unit){
    val showAlert by derivedStateOf {update!=null}
    if(showAlert){
        if(update is APIUpdates.NormalAlert){
            showAPIAlert(
                title = { Text(update.title) },
                text = { Text(update.message) },
                onConfirm = {
                    TextButton(onClick = { saveLast(); resetVal()}) {
                        Text("Okay")
                    }
                },
            )
        }
    }
}

@Composable
private fun showAPIAlert(title:@Composable ()->Unit, text:@Composable ()->Unit, onConfirm: @Composable ()->Unit,onDismiss: @Composable ()->Unit={}){
    AlertDialog(onDismissRequest = { },
        confirmButton = onConfirm,
        dismissButton = onDismiss,
        title = title,
        text= text,
        properties = DialogProperties(dismissOnBackPress = false,dismissOnClickOutside = false)
    )
}
