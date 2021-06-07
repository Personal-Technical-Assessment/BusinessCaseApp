package com.gozem.test.businesscase.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Patterns
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.gozem.test.businesscase.R
import com.gozem.test.businesscase.application.appContext
import com.gozem.test.businesscase.utils.Constants.PERMISSIONS
import com.gozem.test.businesscase.utils.Constants.PERMISSION_ALL
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Utils {

    /**
     * This method is used to hash the user password
     */
    fun md5Hash(input: String): String {
        try {
            val md = MessageDigest.getInstance("MD5")
            return BigInteger(1,
                md.digest(input.toByteArray()))
                .toString(16)
                .padStart(32, '0')
        } catch (e: NoSuchAlgorithmException) {
            FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException(e))
        }
        return input
    }

    fun displayToastMessage(message: String, type: String) {
        val toast = Toast.makeText(appContext, message, Toast.LENGTH_LONG)
        if (type == "error") {
            toast.view?.setBackgroundColor(Color.RED)
            toast.show()
        } else {
            toast.show()
        }
    }

    fun changeColorOfPartOfString(context: Context, text: String,
                                  color: Int, startString: Int,
                                  endString: Int, isBoldStyle: Boolean):
            SpannableString {

        val colorBlue: Int = context.resources.getColor(color)
        val modifiedText = SpannableString(text)
        // Here we set the color
        modifiedText.setSpan(
            ForegroundColorSpan(colorBlue),
            startString,
            endString,
            0
        )

        if (isBoldStyle) {
            // Here we set the style to bold
            modifiedText.setSpan(
                StyleSpan(Typeface.BOLD),
                startString,
                endString,
                0
            )
        }

        return modifiedText
    }

    fun requestForPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            PERMISSIONS,
            PERMISSION_ALL
        )
    }

    fun allPermissionsGranted(context: Context?,
                              vararg permissions: String?)
    : Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    fun buildLoginProgressPopUp(activity: Activity): MaterialDialog? {
        try {
            return MaterialDialog.Builder(activity)
                .content(activity.getString(R.string.login_progress_pop_up_texte))
                .title(activity.resources.getString(R.string.app_name))
                .progress(true, 0)
                .build()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException(e))
        }
        return null
    }

    fun validateUserSignInCredentials(context: Context,
                                      email: String?,
                                      password: String?): String {
        if (email.isNullOrEmpty() || email.isNullOrBlank()) {
            return context.getString(
                R.string.required_email_string
            )
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return context.getString(
                R.string.invalid_email_message_string
            )
        }

        if (password.isNullOrEmpty() || password.isNullOrBlank()) {
            return context.getString(
                R.string.required_password_string
            )
        }

        return ""
    }

    fun validateUserSignUpCredentials(context: Context,
                                      fullName: String?,
                                      email: String?,
                                      password: String?,
                                      confirmPass: String?): String {
        if (fullName.isNullOrEmpty() || fullName.isNullOrBlank()) {
            return context.getString(
                R.string.required_fullName_string
            )
        }

        if (email.isNullOrEmpty() || email.isNullOrBlank()) {
            return context.getString(
                R.string.required_email_string
            )
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return context.getString(
                R.string.invalid_email_message_string
            )
        }

        if (password.isNullOrEmpty() || password.isNullOrBlank()) {
            return context.getString(
                R.string.required_password_string
            )
        }

        if (confirmPass.isNullOrEmpty() || confirmPass.isNullOrBlank()) {
            return context.getString(
                R.string.required_password_confirmation_string
            )
        }

        if (password != confirmPass) {
            return context.getString(
                R.string.password_confirmation_error_message
            )
        }

        return ""
    }
}