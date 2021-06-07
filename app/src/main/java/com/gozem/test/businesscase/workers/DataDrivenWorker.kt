package com.gozem.test.businesscase.workers

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.google.gson.GsonBuilder
import com.gozem.test.businesscase.R
import com.gozem.test.businesscase.application.appContext
import com.gozem.test.businesscase.models.DataDriven
import com.gozem.test.businesscase.utils.Constants.DATA_DRIVEN_LIST_JSON
import com.gozem.test.businesscase.utils.Constants.SERVICE_NOT_AVAILABLE_KEY

class DataDrivenWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {
        val (_, response, result) = "/driven"
            .httpGet()
            .responseObject<List<DataDriven>>()
        val dataDrivenList = result.component1()
        return if (dataDrivenList == null) {
            if (response.statusCode == 503) {
                Result.failure(
                    Data.Builder()
                        .putString(
                            SERVICE_NOT_AVAILABLE_KEY,
                            appContext
                                .getString(R.string.service_unavailable_string))
                        .build()
                )
            } else {
                Result.failure()
            }
        } else {
            Result.success(
                Data.Builder()
                    .putString(DATA_DRIVEN_LIST_JSON,
                        GsonBuilder().create().toJson(dataDrivenList))
                    .build()
            )
        }
    }
}