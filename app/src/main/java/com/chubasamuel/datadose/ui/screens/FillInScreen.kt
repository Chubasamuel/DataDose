package com.chubasamuel.datadose.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chubasamuel.datadose.data.local.ProjectDetail
import com.chubasamuel.datadose.data.local.ProjectFilled
import com.chubasamuel.datadose.data.models.DocLine
import com.chubasamuel.datadose.data.models.Types
import com.chubasamuel.datadose.ui.components.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager


@OptIn(ExperimentalPagerApi::class)
@Composable
fun FillerScreen(project_id: Int,tabsCount:Int,saver:(ProjectFilled)->Unit,
                 docs:List<ProjectDetail>){
    HorizontalPager(count = tabsCount) {
        page->
        FillInScreen(project_id = project_id, tab_index = page+1 , saver = saver, docs = docs)
    }
}

@Composable
fun FillInScreen(
    project_id:Int,
    tab_index:Int,
    saver:(ProjectFilled)->Unit,
    docs:List<ProjectDetail>
){
    LazyColumn{
        items(docs.size,{index->docs[index].q_index }){
           count->
            when(docs[count].type){
                Types.Title-> Title(docLine = docs[count])
                Types.SectionLabel-> SectionLabel(docLine =  docs[count])
                Types.Comment-> Comment(docLine = docs[count])
                Types.MCQ-> MCQ(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver)
                Types.MCQRigid-> MCQ(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver,isRigid = true)
                Types.MCQWithFreeForm-> MCQ(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver, isWithFreeForm = true)
                Types.Likert-> Likert(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver)
                Types.FreeFormQuestion-> FreeForm(project_id=project_id,tab_index=tab_index,docLine = docs[count],saver=saver)
            }
            Spacer(Modifier.height(10.dp))
        }

    }
}