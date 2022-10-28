package com.chubasamuel.datadose.util

import android.util.Log
import com.chubasamuel.datadose.data.models.DocLine
import com.chubasamuel.datadose.data.models.Options
import com.chubasamuel.datadose.data.models.Types


fun DocLine.classifyLine():DocLine{
    val l=this.label.trim()
     return when{
         l.startsWith("###") -> this.typify(Types.Title).trimOff(Regex("###"))
         l.startsWith("/*") && l.endsWith("*/") -> this.typify(Types.Comment).trimOff(Regex("/\\*+")).trimOff(Regex("\\*+/"))
         l.startsWith("##") -> this.typify(Types.SectionLabel).trimOff(Regex("##"))
         l.matches(Regex(".*\\*\\*.+$")) -> this.typify(Types.Likert)
         l.matches(Regex(".*(@\\*)+.+$")) -> this.typify(Types.MCQRigid)
         l.matches(Regex(".*@.+@.+$"))&&l.matches(Regex(".*others\\s*\\(\\s*please\\s+specify\\s*\\).*$",RegexOption.IGNORE_CASE)) -> this.typify(Types.MCQWithFreeForm)
         l.contains(Regex("@+")) -> this.typify(Types.MCQ)
         else -> this.typify(Types.FreeFormQuestion)
     }
    }
private fun DocLine.typify(type: Types):DocLine{
    return this.copy(type=type)
}
fun DocLine.isQuestion():Boolean{
    return when(this.type){
        Types.Title,Types.Comment,Types.SectionLabel->false
        else->true
    }
}
private fun DocLine.trimOff(r:Regex):DocLine{
    return this.copy(label=this.label.replace(r,""))
}
fun DocLine.extractOptions():DocLine{
    if(!this.isQuestion())return this
    return when(this.type){
        Types.Likert->{
            val p=breakLast(this.type,this.label)
            this.copy(options=getOptions(Regex("\\s+"),p.second ), label = p.first)
        }
        Types.MCQRigid,Types.MCQ,Types.MCQWithFreeForm->{
            val p=breakLast(this.type,this.label)
            this.copy(options=getOptions(Regex("@+"),p.second ), label = p.first)
        }
        else->this
    }
}
private fun getOptions(separator:Regex,text:String):List<Options>{
    val k=text.split(separator)
    val li= mutableListOf<Options>()
    var count=0
    k.forEach{i->count+=1;li.add(Options(count,i))}
    return li
}
private fun breakLast(type: Types,label:String):Pair<String,String>{
    return when(type){
        Types.Likert->{
            val k=label.split("**")
            Pair(k[0],k.slice(1 until k.size).joinToString())
        }
        Types.MCQRigid->{
            val k=label.split("@*")
            Pair(k[0],k.slice(1 until k.size).joinToString().replace("*",""))
        }
        Types.MCQ,Types.MCQWithFreeForm->{
            val k=label.split("@")
            Pair(k[0],k.slice(1 until k.size).joinToString("@"))
        }
        else->Pair(label,"")
    }
}