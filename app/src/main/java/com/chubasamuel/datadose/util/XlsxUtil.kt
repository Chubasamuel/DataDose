package com.chubasamuel.datadose.util

import android.util.Log
import androidx.activity.ComponentActivity
import com.chubasamuel.datadose.data.local.AppDao
import com.chubasamuel.datadose.data.models.Options
import com.chubasamuel.datadose.data.models.WorkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.BufferedOutputStream


object XlsUtil {
    const val finishedExporting="Finished exporting data to excel"
    fun exportFilledOptionsToXlsx(context:ComponentActivity,project_id:Int,os: BufferedOutputStream,dao: AppDao,isByValue:Boolean):Flow<WorkStatus>{
        return callbackFlow {
            trySend(WorkStatus.Working())
        val scope=CoroutineScope(Dispatchers.IO)
        os.let{
            scope.launch { val wb= Workbook(it,"DataDose","1.0"/*context.packageManager.getPackageInfo(context.packageName,0).versionName*/)
            val ws=wb.newWorksheet("Sheet 1")
            val data=dao.getAllProjectFilled(project_id)
            val indOnlyQues=dao.getAllIndexOnlyNumbers(project_id)
                for(i in indOnlyQues){
                    try{ ws.value(0,i,"Q$i")}catch(e:Exception){e.printStackTrace()}
                }
                  data.collect{
                          li->  for(d in li){
                      val optionsLi= getWhatToWrite(isByValue,d.option)
                      ws.value(d.tab_index,0,"R${d.tab_index}")
                      ws.value(d.tab_index,d.indexOnlyForQuestions,optionsLi.joinToString())
                  }
                      wb.finish()
                      try{
                          os.flush();os.close()
                      }catch(e:Exception){e.printStackTrace()}
                  }
                trySend(WorkStatus.Finished())
                channel.close()
           }
   }
            awaitClose{scope.cancel()}
    }
    }
    private fun getWhatToWrite(isByValue: Boolean,opts:List<Options>):List<String>{
        val optionsLi= mutableListOf<String>()
        if(isByValue){
        for(opt in opts){
            val toWrite=opt.value?:opt.label
            optionsLi.add(toWrite)
        }}
        else{
            for(opt in opts){
                val toWrite=opt.value?:"${opt.index}"
                optionsLi.add(toWrite)
            }
        }
        return optionsLi
    }
}