package com.example.uploadimagetest.repository

import com.example.uploadimagetest.data.CaseData
import com.example.uploadimagetest.service.lohasService
import com.example.uploadimagetest.util.showLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import java.io.File

class CaseRepository {
    val lohasApi = lohasService.service

    suspend fun getCase(tname:String,year:String,unid:String,caseid:String): List<CaseData> {
        return withContext(Dispatchers.IO){
//            showLog(info)
            val response = lohasApi.getCase(tname,year,unid,caseid)
            if(response.isNotEmpty()){
                response
            }
            else{
                showLog(response)
                val errorMessage = "查無資料"
                throw Exception(errorMessage)
            }
        }
    }

    suspend fun uploadImg(filePath:String,caseId:String,multipartZipFile: MultipartBody.Part): Boolean {
        return withContext(Dispatchers.IO){

            val response = lohasApi.uploadFile(filePath,caseId,caseId,multipartZipFile)

            if(response.isSuccessful){
                true
            }
            else{
                showLog(response)
                val errorMessage = "上傳失敗"
                throw Exception(errorMessage)
            }


        }

    }




}