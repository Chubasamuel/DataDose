package com.chubasamuel.datadose.data.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.sqlite.db.SimpleSQLiteQuery
import com.chubasamuel.datadose.data.models.Options
import com.chubasamuel.datadose.data.models.Types
import com.chubasamuel.datadose.data.models.getJSONString

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
    val option:List<Options>,
    val indexOnlyForQuestions: Int,
    val tab_index:Int
)
fun List<ProjectFilled>.groupByQuestionNumber():Map<Int,ProjectFilled>{
    val map= mutableMapOf<Int,ProjectFilled>()
    for(k in this){
        map[k.indexOnlyForQuestions] = k
    }
    return map
}

fun ProjectFilled.getSQLInsertQuery()=ProjectSQLQuery(
    "INSERT OR REPLACE INTO project_filled(id,project_id,q_index,option,indexOnlyForQuestions,tab_index)" +
            " VALUES((SELECT id FROM project_filled WHERE project_id=? AND tab_index=? AND indexOnlyForQuestions=?),?,?,?,?,?)",
    arrayOf("${this.project_id}",
        "${this.tab_index}",
        "${this.indexOnlyForQuestions}",
        "${this.project_id}",
        "${this.q_index}",
        this.option.getJSONString(),
        "${this.indexOnlyForQuestions}",
         "${this.tab_index}")
)

data class ProjectSQLQuery(
    val sql:String,
    val sql_arguments:Array<String?>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectSQLQuery

        if (sql != other.sql) return false
        if (!sql_arguments.contentEquals(other.sql_arguments)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sql.hashCode()
        result = 31 * result + sql_arguments.contentHashCode()
        return result
    }
}
