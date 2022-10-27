package com.chubasamuel.datadose.data.models

enum class Types{
    Title,SectionLabel,FreeFormQuestion,MCQ,MCQRigid,MCQWithFreeForm,Comment,Likert
}

data class DocLine(
    val index:Int=-1,
    val indexOnlyForQuestions:Int=-1,
    val label:String="",
    val type:Types=Types.Comment,
    val options:List<Options>?=null
)
data class Options(
    val index:Int,
    val label:String
)