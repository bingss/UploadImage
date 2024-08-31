package com.example.uploadimagetest.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uploadimagetest.data.BSWebLandData
import com.example.uploadimagetest.data.PointData
import com.example.uploadimagetest.data.PointFeatures
import com.example.uploadimagetest.data.model.UiState
import com.example.uploadimagetest.listener.ProgressListener
import com.example.uploadimagetest.repository.BSWebRepository
import com.example.uploadimagetest.repository.CaseRepository
import com.example.uploadimagetest.repository.ProgressRequestBody
import com.example.uploadimagetest.util.RealPathUtil
import com.example.uploadimagetest.util.showLog
import com.example.uploadimagetest.util.zipPDF
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import org.json.JSONObject

class MapViewModel():ViewModel() {

    private val _repository = BSWebRepository()
    private val _lohasRepository = CaseRepository()

    private val _currentState:MutableLiveData<UiState> = MutableLiveData(UiState.INIT)
    val currentState: LiveData<UiState>
        get() = _currentState

    private val _isMapMode:MutableLiveData<Boolean> = MutableLiveData(true)
    val isMapMode:LiveData<Boolean>
        get() =_isMapMode

    private var _polyJson:JSONObject = JSONObject()
    val polyJson:JSONObject
        get() =_polyJson

    private var _pointJson: JSONObject = JSONObject()
    val pointJson:JSONObject
        get() =_pointJson

    private val _info : MutableLiveData<String> = MutableLiveData<String>()
    val info: LiveData<String>
        get() = _info

    private var _newPointCount = 1

    val pointList:MutableList<PointData> = mutableListOf()

//    private val landIinfo:Map<String,String> = mapOf(
//        "type" to "1", "city" to "B", "town" to "B24","sectno" to "9120",
//        "landno" to "1594","city_name" to "", "town_name" to "","sect_name" to "")
    suspend fun getLandInfo(bundle: BSWebLandData){
        viewModelScope.launch {
            try {
                _currentState.value = UiState.LOADING
                if(_polyJson.length() == 0){
                    val landInfoMap:Map<String,String> = mapOf(
                        "type" to "1", "city" to "B", "town" to bundle.town,"sectno" to bundle.sectno,
                        "landno" to bundle.landno,"city_name" to "", "town_name" to "","sect_name" to "")
                    val landInfoHtml = _repository.getLandInfo(landInfoMap)
                    htmlToJson(landInfoHtml)
                }
                _currentState.value = UiState.IDLE

            }catch (ex:Exception){
                showLog("捕獲錯誤:$ex--$ex.message")
                _info.value = "取得圖資錯誤，自行新增點號上傳"
                _currentState.value = UiState.ERROR
            }
        }
    }

    suspend fun uploadImg(context: Context,bundle: BSWebLandData,listener: ProgressListener){
        viewModelScope.launch {
            try {
                if(pointList.any { it.ImgPath.size > 0 }){
                    _currentState.value = UiState.LOADING
                    val zipFile = zipPDF(context,pointList,bundle.uploadfilename)
                    val requestBody = ProgressRequestBody(MediaType.parse("application/octet-stream") , zipFile,listener)
                    val multipartZipFile = MultipartBody.Part.createFormData("file", zipFile.getName(), requestBody)
                    _lohasRepository.uploadImg("AllFile/${bundle.sectno}",bundle.uploadfilename,multipartZipFile)
                    _info.value = "上傳成功!"
                    _currentState.value = UiState.IDLE
                }
                else{
                    _info.value = "至少新增一張照片"
                }
            }
            catch(ex:Exception){
                showLog("捕獲錯誤:$ex--$ex.message")
                _info.value = "上傳過程錯誤"
                _currentState.value = UiState.ERROR
            }

        }
    }

    fun selectImage(uriList : List<Uri>, currentPointNum:String, context: Context) : Int {
        val pointIdx = pointPosition(currentPointNum)
        if(uriList.isNotEmpty()) {
            uriList.forEach {
                pointList[pointIdx].ImgPath.add( RealPathUtil.getRealPath(context,it)!! )
            }
            return pointIdx
        }
        _info.value = "未選取照片"
        return pointIdx
    }

    fun takePhoto(photoPath: String, currentPointNum: String, context: Context): Int {
        val pointIdx = pointPosition(currentPointNum)
        pointList[pointIdx].ImgPath.add( photoPath )
        return pointIdx
    }

    fun changeMapMode(){
        _isMapMode.value = !_isMapMode.value!!
    }

    private fun htmlToJson(landInfoHtml:String?){
        val landSplit = landInfoHtml?.split("var map_geo_json='","var geo_json='","initMap('map');")
        val polyJsonString = landSplit?.get(1)?.replace("';","")
        val pointJsonString = landSplit?.get(2)?.replace("';","")
        _polyJson = JSONObject(polyJsonString!!)
        _pointJson = JSONObject(pointJsonString!!)

        val pointObj = Gson().fromJson(pointJsonString, PointFeatures::class.java)
        pointObj.features.forEach {
            pointList.add( PointData(it.properties.ATTR, mutableListOf()) )
        }
        pointList.sortBy { it.PointNumber }
    }

    fun addPoint() :String {
        pointList.add( PointData("$${_newPointCount++}", mutableListOf()) )
        return pointList.last().PointNumber
    }

    fun deletePoint(position: Int) :Int {
        pointList.removeAt(position)
        return pointList.size - position
    }

    fun pointPosition(pointNum: String?): Int {
        return pointList.indexOfFirst { it.PointNumber == pointNum }
    }

    fun deleteImg(imgPaths : MutableList<String>,imgPosition: Int,currentPointNum: String) :Int {
        imgPaths.removeAt(imgPosition)
        return imgPaths.size - imgPosition
    }



}