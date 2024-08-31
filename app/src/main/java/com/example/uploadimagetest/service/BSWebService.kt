package com.example.uploadimagetest.service

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.POST

interface BSWebService {
    companion object{
        private const val baseUrl = "https://easymap.land.moi.gov.tw/BSWeb/"
        val service : BSWebService by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit.create(BSWebService::class.java)
        }
    }

    // data:{type:'1',city:'B',town:'B25',sectno:'9336',landno:'544',city_name:'臺中市',town_name:'龍井區',sect_name:'竹師段'}
    @Headers("user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0")
    @FormUrlEncoded
    @POST("GetData_getParcelData")
    suspend fun getLandInfo(
            @FieldMap fields : Map<String,String>
//            @Field("type") type:String,
//            @Field("city") city:String,
//            @Field("town") town:String,
//            @Field("sectno") sectno:String,
//            @Field("landno") landno:String,
//            @Field("city_name") cityname:String,
//            @Field("town_name") townname:String,
//            @Field("sect_name") sectname:String
        ): Response<ResponseBody>

}
