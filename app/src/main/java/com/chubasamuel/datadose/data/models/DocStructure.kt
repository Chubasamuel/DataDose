package com.chubasamuel.datadose.data.models

import com.chubasamuel.datadose.data.local.Converters
import com.chubasamuel.datadose.data.local.ProjectDetail
import com.chubasamuel.datadose.data.local.ProjectFilled

enum class Types{
    Title,SectionLabel,FreeFormQuestion,MCQ,MCQRigid,MCQWithFreeForm,Comment,Likert
}

data class DocLine(
    val index:Int=-1,
    val indexOnlyForQuestions:Int=-1,
    val label:String="",
    val type:Types=Types.Comment,
    val options:List<Options>?=null,
    val id:Int?=-1,
    )
data class Options(
    val index:Int,
    val label:String,
    var value:String?=null
)
fun List<Options>.getJSONString()=Converters().fromLiOfOptions(this)

fun List<DocLine>.toProjectDetailEntities(project_id:Int):List<ProjectDetail>{
    val li= mutableListOf<ProjectDetail>()
    this.forEach { li.add(it.toProjectDetailEntity(project_id)) }
    return li
}
private fun DocLine.toProjectDetailEntity(projectId:Int):ProjectDetail{
    return ProjectDetail(
        project_id=projectId,
        q_index = this.index,
        indexOnlyForQuestions = this.indexOnlyForQuestions,
        label=this.label,
        type=this.type,
        options = this.options
    )
}