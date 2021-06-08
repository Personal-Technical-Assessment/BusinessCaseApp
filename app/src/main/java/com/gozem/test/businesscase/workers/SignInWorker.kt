package com.gozem.test.businesscase.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.gozem.test.businesscase.application.prefs
import com.gozem.test.businesscase.models.User
import com.gozem.test.businesscase.utils.Constants.SIGN_IN_WORKER_USER_ID_DATA

class SignInWorker(context: Context, params: WorkerParameters):
    Worker(context, params) {

    private var userId: String =
        inputData.getInt(SIGN_IN_WORKER_USER_ID_DATA,
            0).toString()

    override fun doWork(): Result {
        var mResult: Result? = null
        val request = "/users/$userId".httpGet()
            .responseObject<User> ()

        val user = request.third.component1()
        mResult = if (user == null) {
            prefs.isSignIn = false
            Result.failure()
        } else {
            prefs.isSignIn = true
            Result.success()
        }

        return mResult
    }
}