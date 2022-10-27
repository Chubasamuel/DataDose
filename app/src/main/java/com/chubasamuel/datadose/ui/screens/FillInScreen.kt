package com.chubasamuel.datadose.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chubasamuel.datadose.data.models.DocLine
import com.chubasamuel.datadose.data.models.Types
import com.chubasamuel.datadose.ui.components.*


@Composable
fun FillInScreen(
    docLines:List<DocLine>
){
    LazyColumn{
        items(docLines.size,{index->docLines[index].index }){
           count->
            when(docLines[count].type){
                Types.Title-> Title(docLine = docLines[count])
                Types.SectionLabel-> SectionLabel(docLine =  docLines[count])
                Types.Comment-> Comment(docLine = docLines[count])
                Types.MCQ-> MCQ(docLine = docLines[count])
                else-> Plain(docLine =  docLines[count])
            }
            Spacer(Modifier.height(10.dp))
        }

    }
}