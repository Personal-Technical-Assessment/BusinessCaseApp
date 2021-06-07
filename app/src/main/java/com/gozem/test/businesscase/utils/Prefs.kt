package com.gozem.test.businesscase.utils

import android.content.Context
import android.content.SharedPreferences
import com.gozem.test.businesscase.R
import com.gozem.test.businesscase.utils.Constants.appName

class Prefs(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(appName, Context.MODE_PRIVATE)

    var authUserInfo: String?
        get() = sharedPreferences.getString(
            context.getString(R.string.auth_user_info_key),
            "")
        set(value) = sharedPreferences.edit().putString(
            context.getString(R.string.auth_user_info_key),
            value).apply()

    var isSignIn: Boolean
        get() = sharedPreferences.getBoolean(
            context.getString(R.string.user_connected_key),
            false)
        set(value) = sharedPreferences.edit().putBoolean(
            context.getString(R.string.user_connected_key),
            value).apply()
}