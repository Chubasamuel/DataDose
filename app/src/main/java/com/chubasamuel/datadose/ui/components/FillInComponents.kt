package com.chubasamuel.datadose.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.chubasamuel.datadose.data.models.DocLine


@OptIn(ExperimentalUnitApi::class)
@Composable
fun Title(docLine: DocLine){
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
        Text(docLine.label,Modifier.fillMaxWidth(),style= TextStyle(
            fontSize = TextUnit(6f, TextUnitType.Em),
            fontWeight = FontWeight.Bold
        ))
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun SectionLabel(docLine: DocLine){
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
        Text(docLine.label,Modifier.fillMaxWidth(),style= TextStyle(
            fontSize = TextUnit(4f, TextUnitType.Em),
            fontWeight = FontWeight.Bold
        ))
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun Plain(docLine: DocLine){
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
        Text(docLine.label,Modifier.fillMaxWidth(),style= TextStyle(
            fontSize = TextUnit(2.5f, TextUnitType.Em)
        ))
    }
}