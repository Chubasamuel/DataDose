package com.chubasamuel.datadose.data.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chubasamuel.datadose.data.models.Options
import com.chubasamuel.datadose.data.models.Types

@Keep
@Entity(tableName="projects")
class Projects(
    @PrimaryKey(autoGenerate = true) val id:Int?=null,
    val title:String,
    val date_loaded:Long
)
@Keep
@Entity(tableName = "project_detail")
class ProjectDetail(
    @PrimaryKey(autoGenerate = true) val id:Int?=null,
    val project_id: Int,
    val q_index:Int,
    val indexOnlyForQuestions:Int,
    val label:String="",
    val type: Types,
    val options:List<Options>?=null
)
@Keep
@Entity(tableName="project_filled")
class ProjectFilled(
    @PrimaryKey(autoGenerate = true) val id:Int?=null,
    val project_id:Int,
    val q_index: Int,
    val option:Options,
    val indexOnlyForQuestions: Int
)
