package com.chubasamuel.datadose.util

import com.chubasamuel.datadose.data.models.DocLine
import com.chubasamuel.datadose.data.models.Options
import com.chubasamuel.datadose.data.models.Types


fun DocLine.classifyLine():DocLine{
    val l=this.label.trim()
     return when{
         l.startsWith("###")->this.typify(Types.Title)
         l.startsWith("/*") && l.endsWith("*/")->this.typify(Types.Comment)
         l.startsWith("##")->this.typify(Types.SectionLabel)
         l.matches(Regex(".*\\*\\*.+$",RegexOption.IGNORE_CASE))->this.typify(Types.Likert)
         l.contains(Regex("(@*)+.+$"))->this.typify(Types.MCQRigid)
         l.matches(Regex(".*@.+@.+$"))&&l.matches(Regex(".*others(please\\s+specify)$"))->this.typify(Types.MCQWithFreeForm)
         l.contains(Regex("@+"))->this.typify(Types.MCQ)
         else->this.typify(Types.FreeFormQuestion)
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
fun DocLine.extractOptions():DocLine{
    if(this.isQuestion())return this
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
        Types.MCQ->{
            val k=label.split("@")
            Pair(k[0],k.slice(1 until k.size).joinToString())
        }
        Types.MCQWithFreeForm->{
            val k=label.split("@@")
            Pair(k[0],k.slice(1 until k.size-1).joinToString())
        }
        else->Pair(label,"")
    }
}