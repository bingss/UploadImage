package com.example.uploadimagetest.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uploadimagetest.data.BSWebLandData
import com.example.uploadimagetest.data.CaseData
import com.example.uploadimagetest.data.Tname
import com.example.uploadimagetest.data.TownData
import com.example.uploadimagetest.data.model.UiState
import com.example.uploadimagetest.repository.CaseRepository
import com.example.uploadimagetest.util.getJson
import com.example.uploadimagetest.util.showLog
import kotlinx.coroutines.launch

class CaseViewModel : ViewModel() {
    private val repository = CaseRepository()

    private val _currentState: MutableLiveData<UiState> = MutableLiveData(UiState.INIT)
    val currentState: LiveData<UiState>
        get() = _currentState

    private var _BSWebinfo: BSWebLandData = BSWebLandData()
    val BSWebinfo: BSWebLandData
        get() = _BSWebinfo

    suspend fun getCase(ChiTname:String,year:String,caseid:String,context: Context){
        _currentState.value = UiState.LOADING
        viewModelScope.launch {
            try {
                _currentState.value = UiState.LOADING
                if(year.isEmpty() || caseid.isEmpty()) throw Exception("案件年度或案件號為空")
                val caseidFormat = String.format("%06d",caseid.toInt())

                val tnameList = getJson<Tname>(context,"Tname.json")
                val tname = tnameList.first { it.ChiName == ChiTname }.TName
                val caseList = repository.getCase(tname,year,"${tname}56", caseidFormat )

                val BSWeblandno = landnoTransForm(caseList)
                val townList = getJson<TownData>(context,"town.json")
                val town = townList.first { it.sceno_2 == caseList.get(0).sectno.substring(0..1) }.town

                _BSWebinfo.town = town
                _BSWebinfo.sectno = caseList.first().sectno
                _BSWebinfo.landno = BSWeblandno
                //113BK56014200_933509930000
                _BSWebinfo.uploadfilename = "${year}${tname}56${caseidFormat}_${_BSWebinfo.sectno}${caseList.first().landno}"
                BSWebinfo.機關 = ChiTname

                _currentState.value = UiState.IDLE
                onCleared()
            }catch (ex:Exception){
                showLog("捕獲錯誤:$ex--$ex.message")
                _currentState.value = UiState.ERROR
            }
        }
    }

    private fun landnoTransForm(caseList:List<CaseData>):String{
        val landno = StringBuilder()
        val regex = Regex("^0+")
        caseList.forEach {
            val landnoMother = it.landno.substring(0..3).replace(regex,"")
            val landnoChild = it.landno.substring(4..7).replace(regex,"")
            landno.append( landnoMother )
            if(landnoChild.isNotEmpty()) landno.append("-$landnoChild,")
            else landno.append( "," )
        }
        return landno.toString()
    }

    override fun onCleared() {
        super.onCleared()
        _currentState.value = UiState.INIT
    }

}