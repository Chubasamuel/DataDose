package com.chubasamuel.datadose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chubasamuel.datadose.data.local.AppDao
import com.chubasamuel.datadose.data.local.ProjectDetail
import com.chubasamuel.datadose.ui.screens.FillInScreen
import com.chubasamuel.datadose.ui.theme.DataDoseTheme
import com.chubasamuel.datadose.util.LineReader
import dagger.hilt.android.AndroidEntryPoint
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var appDao: AppDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DataDoseTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                   Column(Modifier.fillMaxSize()) {
                       Row(Modifier.clickable { getImportFileUri() }){Greeting("Android")}
      //                 Row(Modifier.fillMaxWidth().padding(15.dp)){DoSimple()}
                   }
                   }
            }
        }
    }


    /*@Composable
    private fun DoSimple(){
        val asset=this.assets.open("q_mod.txt").bufferedReader()
        val dP=LineReader(asset)
        val r=dP.read()
        r?.let{FillInScreen(docs = r)}
    }*/

private fun getImportFileUri(){
    val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type= "text/plain"
    }
    startForResult.launch(intent)
}
private fun beginFileImport(uri: Uri){
    val inp=contentResolver.openInputStream(uri)?.bufferedReader()?:return
    val lineReader=LineReader(inp,appDao)
    lineReader.save("DummyName${System.currentTimeMillis()}")
}
    private val startForResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result:ActivityResult->
        val res=result.data
        when(result.resultCode){
            Activity.RESULT_OK->{
                val uri=res?.data
                uri?.let{beginFileImport(it)}
            }
            else->{}
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DataDoseTheme {
        Greeting("Android")
    }
}