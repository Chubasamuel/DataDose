package com.chubasamuel.datadose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
fun MCQ(docLine: DocLine,isRigid:Boolean=false,isWithFreeForm:Boolean=false){
    val selections = remember{mutableStateListOf<Int>()}
    Column(Modifier.fillMaxWidth()){
        Text(docLine.label)
        docLine.options?.let{
            OptionsList(selections=selections,options = it, onClick ={
                ind-> if(isRigid){
                if(selections.isEmpty()){selections.add(ind)}
                else{selections[0]=ind}
                }
                else{ if(selections.contains(ind)){selections.remove(ind)}
                else{selections.add(ind)}}
            }, isLikert = isRigid )
            if(isWithFreeForm){
                var freeText by remember{mutableStateOf("")}
               Row(modifier= Modifier
                   .fillMaxWidth()
                   .padding(10.dp), horizontalArrangement = Arrangement.Center){
                   TextField(modifier=Modifier.fillMaxWidth(0.95f),
                    shape= RoundedCornerShape(15.dp),
                    value = freeText,
                    placeholder={Text("Response...")},
                   maxLines=5,
                   colors=TextFieldDefaults.textFieldColors(disabledTextColor= Color.Transparent,
                       focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                       disabledIndicatorColor = Color.Transparent),
                    onValueChange = { v->freeText=v} )
            }}
        }
    }
}
@Composable
fun FreeForm(docLine: DocLine){
    var freeText by remember{mutableStateOf("")}
    Column(Modifier.fillMaxWidth()){
        Text(docLine.label)
    Row(modifier= Modifier
        .fillMaxWidth()
        .padding(10.dp), horizontalArrangement = Arrangement.Center){
        TextField(modifier=Modifier.fillMaxWidth(0.95f),
            shape= RoundedCornerShape(15.dp),
            value = freeText,
            placeholder={Text("Response...")},
            maxLines=5,
            colors=TextFieldDefaults.textFieldColors(disabledTextColor= Color.Transparent,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent),
            onValueChange = { v->freeText=v} )
    }
    }
}
@Composable
fun Likert(docLine: DocLine){
    val selections = remember{mutableStateListOf<Int>()}
    Column(Modifier.fillMaxWidth()){
        Text(docLine.label)
        docLine.options?.let{
            OptionsList(selections = selections, options = it,
                onClick = {opt->if(selections.isEmpty()){selections.add(opt)}
                else{selections[0]=opt}
            }, isLikert = true)
        }
    }
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun OptionsList(selections:List<Int>,options:List<Options>,onClick:(Int)->Unit,isLikert:Boolean=false){
     FlowRow(Modifier.fillMaxWidth()){
        for(i in options){
            CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                if(isLikert){
                    RadioButton(selected = selections.contains(i.index), onClick = { onClick(i.index) })
                }else{
                Checkbox(checked =selections.contains(i.index) , onCheckedChange ={onClick(i.index)} )
            }}
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