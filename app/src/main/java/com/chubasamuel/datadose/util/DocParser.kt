package com.chubasamuel.datadose.util

import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chubasamuel.datadose.data.models.DocLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader

class LineReader(bReader: BufferedReader?) {
    private val reader:BufferedReader?= bReader
    fun read():List<DocLine>?{
        var index=0
        var indexOnlyQuestion=0
        val res= mutableListOf<DocLine>()
        if(reader==null)return null
        var line: String?
       return try{
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
           res
        }catch(e:Exception){
            e.printStackTrace()
           null
        }
        finally {
            reader.close()
        }
    }
    fun close(){
       try{ reader?.close()}catch(e:Exception){e.printStackTrace()}
    }
    /*
    fun <T>writeMainTasksToDbDirectly(db:SupportSQLiteDatabase,exporter:ExportData<T>){
        if(reader==null)return
        var line: String?
        try{
            db.beginTransaction()
            while(true){
                line=reader.readLine()
                if(line!=null&&line.isEmpty())continue
                if(line==null)break

                val array= line.split(CSVWriter.DEFAULT_SEPARATOR).toTypedArray()
                val task=MainTasksEntity(startTime = array[0].toLong(),
                    task=array[1].removeSpecialChars(),duration = array[2].toInt())
                db.execSQL(BackupUtil.sqlForImportMainTasks,
                    arrayOf(task.task,task.startTime,task.duration,task.task,task.startTime,task.duration))
            }
            reader.close()
            db.setTransactionSuccessful();db.endTransaction()}
        catch(e:Exception){
            try{reader.close();db.setTransactionSuccessful();db.endTransaction()}catch(e:Exception){e.printStackTrace()}
            CoroutineScope(Dispatchers.Main).launch {
                exporter.isWriting.value= ExportData.WritingState.ERROR}
            e.printStackTrace()
        }
    }
    private fun String.removeSpecialChars():String{
        return this.replace(CSVWriter.ESCAPE_LINE_END,CSVWriter.DEFAULT_LINE_END)
            .replace(CSVWriter.ESCAPE_DEFAULT_SEPARATOR,CSVWriter.DEFAULT_SEPARATOR)
    }
    fun close(){
        try{ reader?.close()}catch(e:Exception){e.printStackTrace()}
    }*/
}