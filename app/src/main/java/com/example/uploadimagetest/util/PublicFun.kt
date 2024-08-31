package com.example.uploadimagetest.util

import android.content.Context
import android.util.Log
import com.example.uploadimagetest.data.Tname
import com.example.uploadimagetest.data.TownData
import com.google.gson.Gson
import com.itextpdf.commons.utils.Base64.InputStream

val TAG = "AAA123"
fun showLog(info : Any?){
    if(info!=null) Log.d(TAG,info.toString())
}

inline fun <reified T> getJson(context: Context, filename:String) : T {
    try{
        val inputStream= context.assets.open(filename)

        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        return Gson().fromJson(String(buffer, charset("UTF-8")) , T::class.java)
    }
    catch(ex:Exception){
        throw ex
    }
}
