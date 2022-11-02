package com.chubasamuel.datadose.ui.components

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.chubasamuel.datadose.data.local.ProjectDetail
import com.chubasamuel.datadose.data.local.ProjectFilled
import com.chubasamuel.datadose.data.models.Options
import com.google.accompanist.flowlayout.FlowRow

val handler=Handler(Looper.getMainLooper())

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
        tab_index:Int,docLine: ProjectDetail,saver:(ProjectFilled)->Unit,isRigid:Boolean=false,isWithFreeForm:Boolean=false,
filled:ProjectFilled?
        ){
    val selections = remember{ mutableStateMapOf<Int,Options>()}
    if(selections.isEmpty()){filled?.let {
        for(i in it.option){
            selections[i.index] = i
        }
    }}
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
                val ft by remember {derivedStateOf{
                    if(filled==null){""}else{
                        if(filled.option.isNotEmpty()){
                            filled.option.last().value?:""
                        }else{
                            ""
                        }}
                }}
                var freeText by remember{ mutableStateOf(ft) }
                var textChanged:Boolean? by remember{ mutableStateOf(null) }
                val runnable by remember {
                    derivedStateOf { Runnable {
                        docLine.options.let{
                           if(docLine.options.isNotEmpty()) {
                               val kk = docLine.options.last()
                               selections[kk.index] = Options(kk.index, "", freeText)
                           }
                        }
                        callSaver(
                            project_id, tab_index,
                            selections = selections, docLine, saver
                        )
                        textChanged=false
                    }}
                }
                fun trySave(){
                    textChanged=true
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable,1200)
                }

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
                    onValueChange = { v->freeText=v;trySave()},
                       trailingIcon = {if(textChanged==true){
                           Icon(Icons.Filled.PauseCircle, null)
                       }else if(textChanged==false){
                           Icon(Icons.Filled.Done,null)
                       } }
                       )
            }}
        }
    }

}
@Composable
fun FreeForm(project_id:Int,
             tab_index:Int,saver:(ProjectFilled)->Unit,docLine: ProjectDetail,filled:ProjectFilled?){
    val ft by remember {derivedStateOf{
        if(filled==null){""}else{
             if(filled.option.isNotEmpty()){
                filled.option[0].value?:""
            }else{
                ""
            }}
    }}
    var freeText by remember{ mutableStateOf(ft) }

    var textChanged:Boolean? by remember{ mutableStateOf(null) }

    val runnable by remember {
        derivedStateOf { Runnable {
            callSaver(project_id,tab_index,
                selections = mapOf(0 to Options(
                    index=0,label="",value=freeText
                )),docLine, saver)
            textChanged=false
        }}
    }
    fun trySave(){
        textChanged=true
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable,1200)
    }

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
            onValueChange = { v->freeText=v; trySave()} ,
        trailingIcon = {if(textChanged==true){
            Icon(Icons.Filled.PauseCircle, null)
        }else if(textChanged==false){
            Icon(Icons.Filled.Done,null)
        } })
    }
    }
}
@Composable
fun Likert(project_id:Int,
           tab_index:Int,saver:(ProjectFilled)->Unit,docLine: ProjectDetail,filled:ProjectFilled?){
    val selections = remember{mutableStateMapOf<Int,Options>()}
    if(selections.isEmpty()){filled?.let {
        for(i in it.option){
            selections[i.index] = i
        }
    }}
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

@Composable
fun showAlert(title:@Composable ()->Unit,text:@Composable ()->Unit,dialogProperties: DialogProperties=DialogProperties(),onConfirm:()->Unit,onCancel:()->Unit){
    AlertDialog(onDismissRequest = { onCancel()},
        confirmButton = {
            TextButton(onClick = { onConfirm()}) {
                Text("Okay")
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancel()}) {
                Text("Cancel")
            }
        },
        title = title,
        text= text,
        properties = dialogProperties
        )
}

@Composable
fun AlertCustomPage(onTextChange:(String)->Unit,freeText:String,placeholder:String,keyboardOptions:KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)){
    Row{
        TextField(modifier=Modifier.fillMaxWidth(0.95f),
            shape= RoundedCornerShape(15.dp),
            value = freeText,
            placeholder={Text(placeholder)},
            maxLines=1,
            colors=TextFieldDefaults.textFieldColors(disabledTextColor= Color.Transparent,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent),
            keyboardOptions = keyboardOptions,
            onValueChange = { v->onTextChange(v);} )
    }
}
private fun callSaver(project_id:Int, tab_index:Int, selections: Map<Int, Options>, docLine: ProjectDetail, saver: (ProjectFilled) -> Unit){
    saver(ProjectFilled(
        project_id=project_id,
        q_index=docLine.q_index,
        option=selections.values.toList(),
        indexOnlyForQuestions = docLine.indexOnlyForQuestions,
        tab_index = tab_index
    ))
}
