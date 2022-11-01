package com.chubasamuel.datadose.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.chubasamuel.datadose.data.local.ProjectDetail
import com.chubasamuel.datadose.data.local.ProjectFilled
import com.chubasamuel.datadose.data.local.groupByQuestionNumber
import com.chubasamuel.datadose.data.models.DocLine
import com.chubasamuel.datadose.data.models.Types
import com.chubasamuel.datadose.ui.components.*
import com.chubasamuel.datadose.util.DCORPrefs
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private var showDialog by mutableStateOf(false)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun FillerScreen(project_id: Int,tabsCount:Int,saver:(ProjectFilled)->Unit,
                 docs:List<ProjectDetail>,
                 getFilled:(project_id:Int,tab_index:Int)-> Flow<List<ProjectFilled>>
){
    Log.w("DCOR DEBUG","--fillerScreen--")

    val pagerState = rememberPagerState()
    val cScope=rememberCoroutineScope()
    /*var justStarting by remember {
        mutableStateOf(true)
    }
    val context=LocalContext.current
    val dcorPrefs by remember {
        derivedStateOf { DCORPrefs(context)}
    }*/
    HorizontalPager(count = tabsCount, state = pagerState) {
        page->
        val scope=CoroutineScope(Dispatchers.IO)
        Column{
            val tIndex by remember { derivedStateOf { page+1 }}
            val filled:MutableMap<Int,ProjectFilled> = remember{ mutableStateMapOf()}

            Text("(Response page $tIndex)",
                Modifier
                    .fillMaxWidth()
                    .clickable { showDialog = true }
                    .background(color = Color(0xFF77BB77))
                    .padding(vertical = 8.dp),style= TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold))
            FillInScreen(project_id = project_id, tab_index = tIndex , saver = {pFilled->filled[pFilled.indexOnlyForQuestions]=pFilled;saver(pFilled)}, docs = docs,filled=filled)
        /*LaunchedEffect(Unit){
            if(justStarting){
                justStarting=false
                val prev=dcorPrefs.check("${project_id}_${tIndex}",-1)
                if(prev>-1){
                    try{ pagerState.scrollToPage(prev)}catch(e:Exception){e.printStackTrace()}
                }
                Log.w("DCOR DEBUG","prev---$prev")
            }else{
            dcorPrefs.saveCurTabIndex("${project_id}_${tIndex}",tIndex-1)}
        }*/
            DisposableEffect(Unit){
          scope.launch {
              val filledData=getFilled(project_id,tIndex)
              filledData.collect{
                  filled.clear();filled.putAll(it.groupByQuestionNumber())
              }
          }
            onDispose { scope.cancel() }
        }
    }}

    if(showDialog){
        var freeText by remember { mutableStateOf("") }
        showAlert(title = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally){
                Text("Enter page number to move to",
                style=TextStyle(fontWeight = FontWeight.Bold)) }},
            text = {
                AlertCustomPage(onTextChange ={s->freeText=s} , freeText = freeText, placeholder = "A number between 1 and $tabsCount")
            },
            onConfirm = {
                val p=tryGetInt(freeText)
                if(p in 1..tabsCount){
                   cScope.launch { pagerState.scrollToPage(p-1) }
                }
                showDialog=false
            },
            onCancel = { showDialog=false}
        )
    }
}
@Composable
fun FillInScreen(
    project_id:Int,
    tab_index:Int,
    saver:(ProjectFilled)->Unit,
    docs:List<ProjectDetail>,
    filled:Map<Int,ProjectFilled>
){
    Log.w("DCOR DEBUG","--fillInScreen--")
    val filledO = remember {filled}
    LazyColumn(Modifier.padding(start=10.dp,end = 10.dp)){
        items(docs.size,{index->docs[index].q_index }){
           count->
            val forLine by remember { derivedStateOf { filledO.getOrDefault(docs[count].indexOnlyForQuestions,null)}}
            when(docs[count].type){
                Types.Title-> Title(docLine = docs[count])
                Types.SectionLabel-> SectionLabel(docLine =  docs[count])
                Types.Comment-> Comment(docLine = docs[count])
                Types.MCQ->
                    showWithNumber(number =docs[count].indexOnlyForQuestions ) {
                    MCQ(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver,filled=forLine)
                    }
                Types.MCQRigid-> showWithNumber(number = docs[count].indexOnlyForQuestions) {
                    MCQ(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver,isRigid = true,filled=forLine)
                }
                Types.MCQWithFreeForm-> showWithNumber(number = docs[count].indexOnlyForQuestions) {
                    MCQ(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver, isWithFreeForm = true,filled=forLine)
                    }
                    Types.Likert-> showWithNumber(number = docs[count].indexOnlyForQuestions) {
                        Likert(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver,filled=forLine)
                    }
                    Types.FreeFormQuestion-> showWithNumber(number = docs[count].indexOnlyForQuestions) {
                        FreeForm(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver,filled=forLine)
                    }
            }
            Spacer(Modifier.height(10.dp))
        }

    }
}

@Composable
private fun showWithNumber(number:Int,content:@Composable () -> Unit ){
    Row(Modifier.fillMaxWidth()){
        Text("Q$number",style=TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFF33AA33)))
        Spacer(Modifier.width(5.dp))
        content()
    }
}

private fun tryGetInt(s:String):Int{
    return try{s.toInt()}
    catch(e:Exception){0}
}