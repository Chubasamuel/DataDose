package com.chubasamuel.datadose.util

import android.util.Log
import com.chubasamuel.datadose.data.local.AppDao
import com.chubasamuel.datadose.data.local.ProjectDetail
import com.chubasamuel.datadose.data.local.Projects
import com.chubasamuel.datadose.data.models.DocLine
import com.chubasamuel.datadose.data.models.WorkStatus
import com.chubasamuel.datadose.data.models.toProjectDetailEntities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader



class LineReader(bReader: BufferedReader?, private val dao:AppDao) {
    private val reader:BufferedReader?= bReader
    fun save(project_name:String): Flow<WorkStatus> {
        return callbackFlow {
            trySend(WorkStatus.Working())
        val scope=CoroutineScope(Dispatchers.IO)
        //val scope2=CoroutineScope(Dispatchers.IO)
       scope.launch {
           dao.insertProject(Projects(title=project_name,
               date_loaded = System.currentTimeMillis()))
           dao.getOneProject(project_name).collect{
               it.id?.let { it2->saveDetails(it2); trySend(WorkStatus.Finished());channel.close() }
           }
       }
            awaitClose { scope.cancel() }
    }}
    private fun saveDetails(project_id:Int){
        var index=0
        var indexOnlyQuestion=0
        val res= mutableListOf<DocLine>()
        if(reader==null)return
        var line: String?
       try{
            while(true) {
                line = reader.readLine()
                if (line != null && line.trim().isEmpty()) continue
                if (line == null) break
                index+=1
                var docL=DocLine(index=index,label = line).classifyLine()
                if(docL.isQuestion()){
                    indexOnlyQuestion+=1
                    docL=docL.extractOptions().copy(indexOnlyForQuestions=indexOnlyQuestion)
                }
                res.add(docL)
                Log.w("DCOR DEBUG","type->${docL.type}, label->${docL.label.take(10)}, options->${docL.options}")
            }
           dao.insertProjectDetails(res.toProjectDetailEntities(project_id))
        }catch(e:Exception){
            e.printStackTrace()
        }
        finally {
            reader.close()
        }
    }
    fun close(){
       try{ reader?.close()}catch(e:Exception){e.printStackTrace()}
    }
}