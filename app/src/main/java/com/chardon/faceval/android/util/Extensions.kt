package com.chardon.faceval.android.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.chardon.faceval.entity.UserInfoUpload

object Extensions {

    /**
     * Extension function to simplify setting an afterTextChanged action to EditText components.
     */
    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    fun UserInfoUpload.toMap(): Map<String, String> {
        return mapOf(
            "id" to id,
            "displayName" to displayName,
            "email" to email,
            "gender" to if (gender == null) "" else gender.toString(),
            "password" to password,
            "status" to (status ?: ""),
        )
    }
}