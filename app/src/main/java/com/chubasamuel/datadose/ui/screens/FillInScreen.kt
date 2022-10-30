package com.chubasamuel.datadose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class)
@Composable
fun FillerScreen(project_id: Int,tabsCount:Int,saver:(ProjectFilled)->Unit,
                 docs:List<ProjectDetail>,
                 getFilled:(project_id:Int,tab_index:Int)-> Flow<List<ProjectFilled>>
){
    HorizontalPager(count = tabsCount) {
        page->
        val scope=CoroutineScope(Dispatchers.IO)
        Column{
            val tIndex by remember { derivedStateOf { page+1 }}
            val filled:MutableMap<Int,ProjectFilled> = remember{ mutableStateMapOf<Int,ProjectFilled>()}
            Text("($tIndex)",Modifier.fillMaxWidth().padding(vertical=8.dp),style= TextStyle(textAlign = TextAlign.Center))
            FillInScreen(project_id = project_id, tab_index = tIndex , saver = saver, docs = docs,filled=filled)
        LaunchedEffect(Unit){
          scope.launch {
              val filledData=getFilled(project_id,tIndex)
              filledData.collect{
                  filled.clear();filled.putAll(it.groupByQuestionNumber())
                  scope.cancel()
              }
          }
        }
    }}
}

@Composable
fun FillInScreen(
    project_id:Int,
    tab_index:Int,
    saver:(ProjectFilled)->Unit,
    docs:List<ProjectDetail>,
    filled:Map<Int,ProjectFilled>
){
    LazyColumn(Modifier.padding(horizontal = 10.dp)){
        items(docs.size,{index->docs[index].q_index }){
           count->
            val forLine by remember { derivedStateOf { filled.getOrDefault(docs[count].indexOnlyForQuestions,null)}}
            when(docs[count].type){
                Types.Title-> Title(docLine = docs[count])
                Types.SectionLabel-> SectionLabel(docLine =  docs[count])
                Types.Comment-> Comment(docLine = docs[count])
                Types.MCQ-> MCQ(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver,filled=forLine)
                Types.MCQRigid-> MCQ(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver,isRigid = true,filled=forLine)
                Types.MCQWithFreeForm-> MCQ(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver, isWithFreeForm = true,filled=forLine)
                Types.Likert-> Likert(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver,filled=forLine)
                Types.FreeFormQuestion-> FreeForm(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver,filled=forLine)
            }
            Spacer(Modifier.height(10.dp))
        }

    }
}