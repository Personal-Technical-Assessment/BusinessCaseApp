package com.gozem.test.businesscase.utils

import android.Manifest
import com.gozem.test.businesscase.R
import com.gozem.test.businesscase.application.appContext

object Constants {
    // Permissions
    val PERMISSIONS: Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    const val PERMISSION_ALL = 19
    const val REQUEST_CHECK_SETTINGS_GPS = 20

    // Application name
    val appName: String = appContext.getString(R.string.app_name)

    // Api base url
    const val API_BASE_URL = "https://60b9f12c80400f00177b743a.mockapi.io/api/v1"

    // Sign in worker input data key
    const val SIGN_IN_WORKER_USER_ID_DATA = "sign_in_worker_user_id_data"

    // Sign up worker input data key
    const val SIGN_UP_WORKER_USER_DATA = "sign_up_worker_user_data"

    // DataDriven worker input data key
    const val DATA_DRIVEN_LIST_JSON = "data_driven_list_json"
    const val SERVICE_NOT_AVAILABLE_KEY = "service_not_available_key"

    // SocketWorker input data key
    const val SOCKET_WORKER_URL_KEY = "socket_worker_url_key"

    // Data driven type
    const val PROFILE_TYPE = "profile"
    const val MAP_TYPE = "map"

    // Toast message type
    const val ERROR_TOAST_TYPE = "error"
    const val INFO_TOAST_TYPE = "info"

    // Socket, user data channel
    const val SOCKET_USER_DATA_CHANNEL = "data"
}