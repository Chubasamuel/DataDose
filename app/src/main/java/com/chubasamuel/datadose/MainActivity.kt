package com.chubasamuel.datadose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chubasamuel.datadose.ui.screens.FillInScreen
import com.chubasamuel.datadose.ui.theme.DataDoseTheme
import com.chubasamuel.datadose.util.LineReader

class MainActivity : ComponentActivity() {
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
                       Row{Greeting("Android")}
                       Row(Modifier.fillMaxWidth().padding(15.dp)){DoSimple()}
                   }
                   }
            }
        }
    }


    @Composable
    private fun DoSimple(){
        val asset=this.assets.open("q_mod.txt").bufferedReader()
        val dP=LineReader(asset)
        val r=dP.read()
        r?.let{FillInScreen(docLines = r)}
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