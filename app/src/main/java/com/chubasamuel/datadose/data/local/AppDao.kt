package com.chubasamuel.datadose.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProject(project:Projects)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProjectDetails(pDetail:List<ProjectDetail>)

   fun insertProjectFilled(database: SupportSQLiteDatabase,sql:ProjectSQLQuery){
   database.execSQL(sql.sql,sql.sql_arguments)
   }
    @Query("SELECT * FROM projects WHERE title=:project_name LIMIT 1")
    fun getOneProject(project_name:String):Flow<Projects>
    @Query("SELECT EXISTS(SELECT title FROM projects WHERE title=:project_name)")
    fun projectExists(project_name:String):Flow<Boolean>
    @Query("SELECT * FROM projects ORDER BY date_loaded DESC")
    fun getProjectsHelper(): Flow<List<Projects>>
    fun getProjects()=getProjectsHelper().distinctUntilChanged()
    @Query("SELECT * FROM projects ORDER BY date_loaded DESC")
    fun getProjectsForCountHelper(): List<Projects>
    @Query("SELECT * FROM project_detail WHERE project_id=:project_id ORDER BY q_index")
    fun getProjectDetail(project_id:Int):Flow<List<ProjectDetail>>
    @Query("SELECT * FROM project_filled WHERE project_id=:project_id and tab_index=:tab_index ORDER BY q_index")
    fun getAllProjectFilledForTabHelper(project_id: Int,tab_index:Int):Flow<List<ProjectFilled>>
    fun getAllProjectFilledForTab(project_id: Int,tab_index:Int)=getAllProjectFilledForTabHelper(project_id, tab_index).distinctUntilChanged()
    @Query("SELECT * FROM project_filled WHERE project_id=:project_id ORDER BY q_index")
    fun getAllProjectFilled(project_id: Int):Flow<List<ProjectFilled>>
    @Query("SELECT COUNT(DISTINCT tab_index) FROM project_filled WHERE project_id=:project_id")
    fun getProjectFilledCountHelper(project_id: Int):Int
    suspend fun getAllProjectFilledCount():Map<Int,Int>{
        val projects=getProjectsForCountHelper()
        val map = mutableMapOf<Int,Int>()
        for(p in projects){
            p.id?.let{
                val c=getProjectFilledCountHelper(it)
                map[it]=c
            }
        }
        return map
    }
    @Query("SELECT indexOnlyForQuestions from project_detail WHERE project_id=:project_id ORDER BY indexOnlyForQuestions")
    fun getAllIndexOnlyNumbers(project_id: Int):Flow<List<Int>>

}