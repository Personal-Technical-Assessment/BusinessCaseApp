package com.gozem.test.businesscase.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.kittinunf.fuel.Fuel
import com.gozem.test.businesscase.application.prefs
import com.gozem.test.businesscase.utils.Constants.SIGN_UP_WORKER_USER_DATA

class SignUpWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

    private val userJSON = inputData
        .getString(SIGN_UP_WORKER_USER_DATA)

    override fun doWork(): Result {
        val request = Fuel.post("/users")
            .header("Content-Type" to "application/json")
            .body("$userJSON")
        return when(request.responseString().third) {
            is com.github.kittinunf.result.Result.Success -> {
                val data = request.responseString().second
                    .body().asString("application/json")
                prefs.authUserInfo = data
                prefs.isSignIn = true
                Result.success()
            }
            is com.github.kittinunf.result.Result.Failure -> {
                Result.failure()
            }
        }
    }
}