package com.chubasamuel.datadose.ui.components

import androidx.compose.foundation.background
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
import com.chubasamuel.datadose.data.local.ProjectDetail
import com.chubasamuel.datadose.data.local.ProjectFilled
import com.chubasamuel.datadose.data.models.Options
import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalUnitApi::class)
@Composable
fun Title(docLine: ProjectDetail){
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
fun SectionLabel(docLine: ProjectDetail){
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
        Text(docLine.label,Modifier.fillMaxWidth(),style= TextStyle(
            fontSize = TextUnit(4f, TextUnitType.Em),
            fontWeight = FontWeight.Bold
        ))
    }
}

@Composable
fun Comment(docLine: ProjectDetail){
    Row(Modifier.fillMaxWidth()){
        Text(text=docLine.label,modifier= Modifier
            .background(color = Color.LightGray, shape = RoundedCornerShape(10.dp))
            .padding(5.dp))
    }
}

@Composable
fun MCQ(project_id:Int,
        tab_index:Int,docLine: ProjectDetail,saver:(ProjectFilled)->Unit,isRigid:Boolean=false,isWithFreeForm:Boolean=false){
    val selections = remember{ mutableStateMapOf<Int,Options>()}
    Column(Modifier.fillMaxWidth()){
        Text(docLine.label)
        docLine.options?.let{
            OptionsList(selections=selections,options = it, onClick ={
                ind-> if(isRigid){
                if(selections.isEmpty()){selections[ind]=it[ind-1]}
                else{selections.clear();selections[ind]=it[ind-1]}
                }
                else{ if(selections.contains(ind)){selections.remove(ind)}
                else{selections[ind]=it[ind-1]}}
                callSaver(project_id,tab_index, selections, docLine, saver)
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
fun FreeForm(project_id:Int,
             tab_index:Int,saver:(ProjectFilled)->Unit,docLine: ProjectDetail){
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
fun Likert(project_id:Int,
           tab_index:Int,saver:(ProjectFilled)->Unit,docLine: ProjectDetail){
    val selections = remember{mutableStateMapOf<Int,Options>()}
    Column(Modifier.fillMaxWidth()){
        Text(docLine.label)
        docLine.options?.let{
            OptionsList(selections = selections, options = it,
                onClick = {opt->if(selections.isEmpty()){
                    selections[opt] = it[opt-1]
                }
                else{selections.clear();selections[opt]=it[opt-1]}
                    callSaver(project_id,tab_index, selections, docLine, saver)
                }, isLikert = true)
        }
    }
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun OptionsList(selections:Map<Int,Options>,options:List<Options>,onClick:(Int)->Unit,isLikert:Boolean=false){
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
private fun callSaver(project_id:Int, tab_index:Int, selections: Map<Int, Options>, docLine: ProjectDetail, saver: (ProjectFilled) -> Unit){
    saver(ProjectFilled(
        id="$tab_index${docLine.indexOnlyForQuestions}".toInt(),
        project_id=project_id,
        q_index=docLine.q_index,
        option=selections.values.toList(),
        indexOnlyForQuestions = docLine.indexOnlyForQuestions
    ))
}
@OptIn(ExperimentalUnitApi::class)
@Composable
fun Plain(docLine: ProjectDetail){
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
        Text(docLine.label,Modifier.fillMaxWidth(),style= TextStyle(
            fontSize = TextUnit(2.5f, TextUnitType.Em)
        ))
    }
}