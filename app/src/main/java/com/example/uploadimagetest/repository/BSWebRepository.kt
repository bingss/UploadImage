package com.example.uploadimagetest.repository

import com.example.uploadimagetest.service.BSWebService
import com.example.uploadimagetest.util.showLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext




class BSWebRepository {

    val BSWebApi = BSWebService.service

    suspend fun getLandInfo(info: Map<String,String>) :String? {
        return withContext(Dispatchers.IO){
//            showLog(info)
            val response = BSWebApi.getLandInfo( info )
            if(response.isSuccessful){
                response.body()?.string()
            }
            else{
                showLog(response)
                var errorMessage = "取得BSWEB土地資訊錯誤,狀態碼:${response.code()}"
                response.body()?.let {
                    errorMessage += ",訊息:${response.message()}"
                }
                throw Exception(errorMessage)
            }
        }
    }
}