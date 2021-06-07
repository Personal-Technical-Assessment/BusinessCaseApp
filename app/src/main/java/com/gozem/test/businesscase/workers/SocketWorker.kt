package com.gozem.test.businesscase.workers

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.gozem.test.businesscase.application.App.Companion.socket
import com.gozem.test.businesscase.utils.Constants.SOCKET_WORKER_URL_KEY
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.OkHttpClient
import timber.log.Timber
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SocketWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {
        // Initialize the url of the webSocketClient
        val socketUrl = inputData.getString(SOCKET_WORKER_URL_KEY)

        try {
            val hostnameVerifier = HostnameVerifier { _, _ -> true }

            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers():
                        Array<X509Certificate?> = arrayOfNulls(0)

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<X509Certificate>,
                                                authType: String) {}

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<X509Certificate>,
                                                authType: String) {}
            })
            val trustManager = trustAllCerts[0] as X509TrustManager

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, null)
            val sslSocketFactory = sslContext.socketFactory
            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .hostnameVerifier(hostnameVerifier)
                .sslSocketFactory(sslSocketFactory, trustManager)
                .build()

            val opts = IO.Options()
            opts.callFactory = okHttpClient
            opts.webSocketFactory = okHttpClient

            socket = IO.socket(socketUrl, opts)
            socket?.let { it ->
                it.off()
                it.apply {
                    on(Socket.EVENT_CONNECT, onConnect)
                    on(Socket.EVENT_DISCONNECT, onDisconnect)
                    on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                    on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
                }
                it.connect()
            }

            return Result.success()
        } catch (e: Exception){
            FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException(e))

            return Result.failure()
        }
    }

    private val onConnect = Emitter.Listener {
        Timber.i("Connected to socket server!")
    }

    private val onDisconnect = Emitter.Listener {
        Timber.i("Disconnected from socket server! ")
    }

    private val onConnectError = Emitter.Listener { objects ->
        Timber.e("Failed to connect to the socket server")
        FirebaseCrashlytics.getInstance()
            .recordException(RuntimeException(objects[0].toString()))
    }
}