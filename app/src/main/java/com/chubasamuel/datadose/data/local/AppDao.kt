package com.chubasamuel.datadose.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

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
    fun getProjects(): Flow<List<Projects>>
    @Query("SELECT * FROM project_detail WHERE project_id=:project_id ORDER BY q_index")
    fun getProjectDetail(project_id:Int):Flow<List<ProjectDetail>>
    @Query("SELECT * FROM project_filled WHERE project_id=:project_id and tab_index=:tab_index ORDER BY q_index")
    fun getAllProjectFilledForTab(project_id: Int,tab_index:Int):Flow<List<ProjectFilled>>
    @Query("SELECT * FROM project_filled WHERE project_id=:project_id ORDER BY q_index")
    fun getAllProjectFilled(project_id: Int):Flow<List<ProjectFilled>>
}