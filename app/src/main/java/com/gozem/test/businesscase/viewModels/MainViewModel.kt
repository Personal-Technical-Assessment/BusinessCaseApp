package com.gozem.test.businesscase.viewModels

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.gozem.test.businesscase.R
import com.gozem.test.businesscase.application.App.Companion.socket
import com.gozem.test.businesscase.application.appContext
import com.gozem.test.businesscase.application.prefs
import com.gozem.test.businesscase.models.DataDriven
import com.gozem.test.businesscase.models.User
import com.gozem.test.businesscase.utils.AppState
import com.gozem.test.businesscase.utils.Constants
import com.gozem.test.businesscase.utils.Constants.DATA_DRIVEN_LIST_JSON
import com.gozem.test.businesscase.utils.Constants.ERROR_TOAST_TYPE
import com.gozem.test.businesscase.utils.Constants.INFO_TOAST_TYPE
import com.gozem.test.businesscase.utils.Constants.SIGN_UP_WORKER_USER_DATA
import com.gozem.test.businesscase.utils.Constants.SOCKET_USER_DATA_CHANNEL
import com.gozem.test.businesscase.utils.Constants.SOCKET_WORKER_URL_KEY
import com.gozem.test.businesscase.utils.Utils.buildLoginProgressPopUp
import com.gozem.test.businesscase.utils.Utils.displayToastMessage
import com.gozem.test.businesscase.workers.DataDrivenWorker
import com.gozem.test.businesscase.workers.SignInWorker
import com.gozem.test.businesscase.workers.SignUpWorker
import com.gozem.test.businesscase.workers.SocketWorker
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import timber.log.Timber
import java.net.URI
import javax.net.ssl.SSLSocketFactory

class MainViewModel: ViewModel() {
    private var loginProgressPopUp: MaterialDialog? = null
    var appState: MutableLiveData<AppState> = MutableLiveData()
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var dataDrivenLiveData: MutableLiveData<List<DataDriven>> = MutableLiveData()
    var dataValue: MutableLiveData<String> = MutableLiveData()
    private lateinit var webSocketClient: WebSocketClient

    fun start(activity: Activity) {
        if (prefs.isSignIn) {
            appState.postValue(AppState.SIGN_IN_SUCCESS)
        } else {
            appState.postValue(AppState.SIGN_IN_FAILURE)
        }
        buildPopUp(activity)
    }

    fun buildPopUp(activity: Activity) {
        loginProgressPopUp = buildLoginProgressPopUp(activity)
    }

    fun checkUserCredentials(lifecycleOwner: LifecycleOwner,
                             email: String, password: String) {
        try {
            val user: User = GsonBuilder().create()
                .fromJson(prefs.authUserInfo, User::class.java)

            when {
                user.email != email -> {
                    errorMessage.postValue(
                        appContext.getString(R.string.invalid_email_message_string))
                }
                user.password != password -> {
                    errorMessage.postValue(
                        appContext.getString(R.string.invalid_password_error_string))
                }
                else -> {
                    requestSignIn(lifecycleOwner, user.id)
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException(e))
        }
    }

    private fun requestSignIn(lifecycleOwner: LifecycleOwner,
                              userId: Int) {
        try {
            // Display loading pop up
            loginProgressPopUp?.show()

            // Build worker data
            val inputData =
                Data.Builder()
                    .putInt(Constants.SIGN_IN_WORKER_USER_ID_DATA,
                        userId)
                    .build()

            // Initialize worker
            val signInWorker =
                OneTimeWorkRequest.Builder(SignInWorker::class.java)
                    .setInputData(inputData)
                    .build()

            // Enqueue worker
            WorkManager.getInstance().enqueue(signInWorker)
            WorkManager.getInstance()
                .getWorkInfoByIdLiveData(signInWorker.id)
                .observe(lifecycleOwner, { workInfo ->
                    if (workInfo != null && workInfo.state.isFinished) {
                        Handler(Looper.getMainLooper()).post {
                            loginProgressPopUp?.dismiss()
                        }
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            appState.postValue(AppState.SIGN_IN_SUCCESS)
                        } else {
                            errorMessage.postValue(
                                appContext.getString(R.string.user_not_found_message))
                            appState.postValue(AppState.SIGN_IN_FAILURE)
                        }
                    }
                })
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException(e))
        }
    }

    fun requestSignUp(lifecycleOwner: LifecycleOwner, user: User) {
        try {
            // Display loading pop up
            loginProgressPopUp?.show()

            // Build worker data
            val inputData =
                Data.Builder()
                    .putString(SIGN_UP_WORKER_USER_DATA,
                        GsonBuilder().create().toJson(user))
                    .build()

            // Initialize worker
            val signUpWorker =
                OneTimeWorkRequest.Builder(SignUpWorker::class.java)
                    .setInputData(inputData)
                    .build()

            // Enqueue worker
            WorkManager.getInstance().enqueue(signUpWorker)
            WorkManager.getInstance()
                .getWorkInfoByIdLiveData(signUpWorker.id)
                .observe(lifecycleOwner, { workInfo ->
                    if (workInfo != null && workInfo.state.isFinished) {
                        Handler(Looper.getMainLooper()).post {
                            loginProgressPopUp?.dismiss()
                        }
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            appState.postValue(AppState.SIGN_IN_SUCCESS)
                        } else {
                            appState.postValue(AppState.SIGN_IN_FAILURE)
                        }
                    }
                })
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException(e))
        }
    }

    fun fetchDataDriven(lifecycleOwner: LifecycleOwner) {
        try {
            // Display loading pop up
            loginProgressPopUp?.setContent(
                appContext.getString(R.string.fetch_data_driven_string)
            )
            loginProgressPopUp?.show()

            // Initialize worker
            val dataDrivenWorker =
                OneTimeWorkRequest.Builder(DataDrivenWorker::class.java)
                    .build()

            // Enqueue worker
            WorkManager.getInstance().enqueue(dataDrivenWorker)
            WorkManager.getInstance()
                .getWorkInfoByIdLiveData(dataDrivenWorker.id)
                .observe(lifecycleOwner, {
                    if (it != null && it.state.isFinished) {
                        Handler(Looper.getMainLooper()).post {
                            loginProgressPopUp?.dismiss()
                        }
                        if (it.state == WorkInfo.State.SUCCEEDED) {
                            dataDrivenLiveData.postValue(
                                GsonBuilder().create().fromJson(
                                    it.outputData.getString(DATA_DRIVEN_LIST_JSON),
                                    object : TypeToken<ArrayList<DataDriven?>?>(){}.type
                                )
                            )
                        } else {
                            val message = it.outputData
                                .getString(Constants.SERVICE_NOT_AVAILABLE_KEY)
                            if (message.isNullOrEmpty()) {
                                dataDrivenLiveData.postValue(null)
                            } else {
                                displayToastMessage(message, INFO_TOAST_TYPE)
                            }
                        }
                    }
                })
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException(e))
        }
    }

    fun initSocketIOClient(lifecycleOwner: LifecycleOwner,
                           socketUrl: String) {
        if (socketUrl.contains("wss")) {
            initWebSocketClient(socketUrl)
            return
        }

        // Build worker data
        val inputData =
            Data.Builder()
                .putString(SOCKET_WORKER_URL_KEY,
                    socketUrl)
                .build()

        // Initialize worker
        val socketWorker =
            OneTimeWorkRequest.Builder(SocketWorker::class.java)
                .setInputData(inputData)
                .build()

        // Enqueue worker
        WorkManager.getInstance().enqueue(socketWorker)
        WorkManager.getInstance().getWorkInfoByIdLiveData(socketWorker.id)
            .observe(lifecycleOwner, { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        socket?.on(
                            SOCKET_USER_DATA_CHANNEL
                        ) { dataValue.postValue(it[0] as String) }
                    } else {
                        displayToastMessage(
                            appContext.getString(
                                R.string.socket_connection_error_message),
                            ERROR_TOAST_TYPE
                        )
                    }
                }
            })
    }

    private fun initWebSocketClient(url: String) {
        // Initialize webSocketClient
        webSocketClient = object : WebSocketClient(URI(url)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Timber.i("onOpen: $handshakedata")
            }

            override fun onMessage(message: String?) {
                dataValue.postValue(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Timber.i(
                    "onClose, code: $code, reason: $reason, remote: $remote"
                )
            }

            override fun onError(ex: java.lang.Exception?) {
                FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException())
            }
        }
        val socketFactory: SSLSocketFactory =
            SSLSocketFactory.getDefault() as SSLSocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }
}