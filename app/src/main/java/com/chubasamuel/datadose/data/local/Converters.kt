package com.chubasamuel.datadose.data.local

import androidx.room.TypeConverter
import com.chubasamuel.datadose.data.models.Options
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Converters {
    @TypeConverter
    fun fromOptions(value: Options?)=if(value==null)null else Gson().toJson(value)
    @TypeConverter
    fun toOptions(value: String?):Options?{
        value?:return null
        return Gson().fromJson(value,Options::class.java)
    }
    @TypeConverter
    fun fromLiOfOptions(value:List<Options>?)=if(value==null) null else Gson().toJson(value)
    @TypeConverter
    fun toLiOfOptions(value:String?):List<Options>?{
        value?:return null
        val type=TypeToken.getParameterized(List::class.java,Options::class.java).type
        return Gson().fromJson(value,type)
    }
}