package com.chubasamuel.datadose.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chubasamuel.datadose.data.local.ProjectDetail
import com.chubasamuel.datadose.data.models.DocLine
import com.chubasamuel.datadose.data.models.Types
import com.chubasamuel.datadose.ui.components.*


@Composable
fun FillInScreen(
    docs:List<ProjectDetail>
){
    LazyColumn{
        items(docs.size,{index->docs[index].q_index }){
           count->
            when(docs[count].type){
                Types.Title-> Title(docLine = docs[count])
                Types.SectionLabel-> SectionLabel(docLine =  docs[count])
                Types.Comment-> Comment(docLine = docs[count])
                Types.MCQ-> MCQ(docLine = docs[count])
                Types.MCQRigid-> MCQ(docLine = docs[count],isRigid = true)
                Types.MCQWithFreeForm-> MCQ(docLine = docs[count], isWithFreeForm = true)
                Types.Likert-> Likert(docLine = docs[count])
                Types.FreeFormQuestion-> FreeForm(docLine = docs[count])
                else-> Plain(docLine =  docs[count])
            }
            Spacer(Modifier.height(10.dp))
        }

    }
}