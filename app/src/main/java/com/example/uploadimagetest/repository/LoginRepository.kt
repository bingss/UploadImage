package com.example.uploadimagetest.repository

import com.example.uploadimagetest.data.DeviceData
import com.example.uploadimagetest.data.UserData
import com.example.uploadimagetest.service.ltgisService
import com.example.uploadimagetest.util.showLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.function.Predicate

class LoginRepository {

    val ltgisApi = ltgisService.service

    suspend fun login(account:String, password:String) :UserData {
        return withContext(Dispatchers.IO){
//            showLog(info)
            val response = ltgisApi.login( account,password )
            if(response.isNotEmpty()){
                val userNow = response.firstOrNull { it.帳號 == account && it.密碼 == password }
                if(userNow == null){
                    val errorMessage = "帳號密碼錯誤"
                    throw Exception(errorMessage)
                }
                userNow
            }
            else{
                showLog(response)
                val errorMessage = "帳號密碼錯誤"
                throw Exception(errorMessage)
            }
        }
    }

    suspend fun updateDevice(device : DeviceData): DeviceData {
        return withContext(Dispatchers.IO){
//            showLog(info)
            val response = ltgisApi.updateDevice( device )
            if(response != null){
                response
            }
            else{
                showLog(response)
                val errorMessage = "更新裝置錯誤"
                throw Exception(errorMessage)
            }
        }
    }

    suspend fun updateTime(userData: UserData): Boolean {
        return withContext(Dispatchers.IO){
//            showLog(info)
            val response = ltgisApi.updateTime( userData.ObjID.toString(),userData )
            if(response.isSuccessful){
                true
            }
            else{
                showLog(response)
                var errorMessage = "ltgisApi更新時間錯誤,狀態碼:${response.code()}"
                response.body()?.let {
                    errorMessage += ",訊息:${response.message()}"
                }
                throw Exception(errorMessage)
            }
        }
    }

}