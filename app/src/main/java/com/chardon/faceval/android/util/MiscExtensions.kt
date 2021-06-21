package com.chardon.faceval.android.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.chardon.faceval.android.R
import com.chardon.faceval.android.ui.recordlist.ListItem
import com.chardon.faceval.android.util.Base64Util.base64ToBitmap
import com.chardon.faceval.entity.*
import com.chardon.faceval.util.detectionmodelutils.Utils.toPosSet
import java.util.*

object MiscExtensions {

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

    fun UserInfoUpload.toMap(): Map<String, String> = mapOf(
        "id" to id,
        "displayName" to displayName,
        "email" to email,
        "gender" to if (gender == null) "" else gender.toString(),
        "password" to password,
        "status" to (status ?: ""),
    )

    fun PhotoInfoUploadBase64.toMap(): Map<String, String> = mapOf(
        "id" to id,  // User Name
        "password" to password,
        "image" to image,
        "ext" to ext,
        "score" to score.toString(),
        "title" to (title ?: ""),
        "description" to (description ?: ""),
        "positions" to positions,
    )

    fun PhotoInfoUpdate.toMap(): Map<String, String> = mapOf(
        "description" to (description ?: ""),
        "id" to id,
        "password" to password,
        "photoId" to photoId.toString(),
        "score" to score.toString(),
        "title" to (title ?: ""),
    )

    fun ScoringModelBase64.toMap(): Map<String, String> = mapOf(
        "bimg" to bimg,
        "ext" to ext,
        "posSet" to posSet,
    )

    fun DetectionModelBase64.toMap(): Map<String, String> = mapOf(
        "bimg" to bimg,
        "ext" to ext,
    )

    fun DetectionResult.convertForScoring(detectionModel: DetectionModelBase64): ScoringModelBase64 {
        return ScoringModelBase64(
            bimg = detectionModel.bimg,
            ext = detectionModel.ext,
            posSet = this.toPosSet(),
        )
    }

    fun PhotoInfo.toListItem(): ListItem {
        return ListItem(
            photoId = this.id,
            bitmap = if (this.base.isEmpty()) null else this.base.base64ToBitmap(),
            title = (this.title?.let { if (it.isBlank()) "No title" else it } ?: ""),
            score = this.score,
            dateAdded = dateAdded,
        )
    }

    fun Collection<PhotoInfo>.toListItemList(): List<ListItem> {
        return LinkedList<ListItem>().apply {
            for (item in this@toListItemList) {
                this.add(item.toListItem())
            }
        }
    }
}