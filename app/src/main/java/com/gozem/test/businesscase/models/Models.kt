package com.gozem.test.businesscase.models

import java.util.*

data class User(val id: Int = 0, var fullName: String, var email: String,
                var password: String,
                val createdAt: String = Calendar.getInstance()
                    .time.toString())

data class DataDriven(var type: String, var content: Map<String, Any>)