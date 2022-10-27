package com.chubasamuel.datadose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.chubasamuel.datadose.data.models.DocLine
import com.chubasamuel.datadose.data.models.Options
import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalUnitApi::class)
@Composable
fun Title(docLine: DocLine){
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
        Text(docLine.label,Modifier.fillMaxWidth(),style= TextStyle(
            fontSize = TextUnit(5f, TextUnitType.Em),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
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
fun Comment(docLine: DocLine){
    Row(Modifier.fillMaxWidth()){
        Text(text=docLine.label,modifier= Modifier
            .background(color = Color.LightGray, shape = RoundedCornerShape(10.dp))
            .padding(5.dp))
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun MCQ(docLine: DocLine){
    val selections = remember{mutableStateListOf<Int>()}
    Column(Modifier.fillMaxWidth()){
        Text(docLine.label)
        docLine.options?.let{
            OptionsList(selections=selections,options = it, onClick ={
                ind->if(selections.contains(ind)){selections.remove(ind)}
                else{selections.add(ind)}
            } )
        }
    }
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun OptionsList(selections:List<Int>,options:List<Options>,onClick:(Int)->Unit){
     FlowRow(Modifier.fillMaxWidth()){
        for(i in options){
          CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
              Checkbox(checked =selections.contains(i.index) , onCheckedChange ={onClick(i.index)} )
          }
            Spacer(Modifier.width(1.dp))
            Text(i.label)
            Spacer(Modifier.width(15.dp))
        }
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