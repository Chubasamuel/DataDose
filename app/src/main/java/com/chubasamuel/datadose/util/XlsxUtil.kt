package com.chubasamuel.datadose.util

import android.util.Log
import androidx.activity.ComponentActivity
import com.chubasamuel.datadose.data.local.AppDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dhatim.fastexcel.Workbook
import java.io.BufferedOutputStream


object XlsUtil {
    const val finishedExporting="Finished exporting data to excel"
    fun exportFilledOptionsToXlsx(context:ComponentActivity,project_id:Int,os: BufferedOutputStream,dao: AppDao){
        val scope=CoroutineScope(Dispatchers.IO)
        os.let{
            scope.launch { val wb= Workbook(it,"DataDose","1.0"/*context.packageManager.getPackageInfo(context.packageName,0).versionName*/)
            val ws=wb.newWorksheet("Sheet 1")

            val data=dao.getAllProjectFilled(project_id)

               data.collect{
                 li->  for(d in li){
                     val optionsLi= mutableListOf<String>()
                     for(opt in d.option){
                         val toWrite=opt.value?:opt.label
                         optionsLi.add(toWrite)
                     }
                   ws.value(d.tab_index,d.indexOnlyForQuestions,optionsLi.joinToString())
                   }
                   wb.finish()
                   try{
                       os.flush();os.close()
                   }catch(e:Exception){e.printStackTrace()}
               }
           }
   }
    }
}