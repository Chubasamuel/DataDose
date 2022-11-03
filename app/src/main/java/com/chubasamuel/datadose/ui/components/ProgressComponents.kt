package com.chubasamuel.datadose.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun indeterminateProgress(text:String?=null,onDismiss:()->Unit={}){
    AlertDialog(onDismissRequest = { onDismiss() },
        buttons={},
        text={ Text(text?:"")},
        properties= DialogProperties(dismissOnBackPress = false,dismissOnClickOutside = false)
    )
}