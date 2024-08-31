package com.example.uploadimagetest.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedSharedHelper(context: Context, fileName: String) {

    private val masterKey:MasterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    private var sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        fileName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    );
    private var editor: SharedPreferences.Editor = sharedPreferences.edit();

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putBoolean(key: String?, value: Boolean) {
        editor.putBoolean(key, value)
    }

    fun getString(key: String): String {
        return sharedPreferences.getString(key, "")!!
    }

    fun putString(key: String?, value: String?) {
        editor.putString(key, value)
    }

    fun remove(key: String?) {
        editor.remove(key)
    }

    fun apply() {
        editor.apply()
    }
}