package com.example.uploadimagetest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uploadimagetest.data.DeviceData
import com.example.uploadimagetest.data.UserData
import com.example.uploadimagetest.data.model.UiState
import com.example.uploadimagetest.repository.LoginRepository
import com.example.uploadimagetest.util.EncryptedSharedHelper
import com.example.uploadimagetest.util.showLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class LoginViewModel : ViewModel() {
    private val repository = LoginRepository()

    private val _currentState:MutableLiveData<UiState> = MutableLiveData(UiState.INIT)
    val currentState: LiveData<UiState>
        get() = _currentState

    private var _userinfo: UserData = UserData()
    val userinfo: UserData
        get() = _userinfo



    suspend fun login(account:String, password:String,isRemember:Boolean,encryptedShared: EncryptedSharedHelper){
        viewModelScope.launch {
            try {
                _currentState.value = UiState.LOADING
                val user = repository.login(account,password)

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                sdf.timeZone = TimeZone.getTimeZone("Asia/Taipei")
                val nowTime = sdf.format(Date())

                val device = repository.updateDevice(DeviceData(0,user.帳號,nowTime,"行動裝置"))
                user.最後登入時間 = nowTime

                repository.updateTime( user )

                _userinfo = user

                if(isRemember)
                {
                    encryptedShared.putString("account",account)
                    encryptedShared.putString("password",password)
                    encryptedShared.putBoolean("remember",true)
                }
                else{
                    encryptedShared.putBoolean("remember",false)
                }
                encryptedShared.apply()
                _currentState.value = UiState.IDLE
                onCleared()
            }catch (ex:Exception){
                showLog("捕獲錯誤:$ex--$ex.message")
                _currentState.value = UiState.ERROR
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _currentState.value = UiState.INIT
    }
}