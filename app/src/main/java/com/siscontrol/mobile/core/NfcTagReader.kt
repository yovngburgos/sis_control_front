package com.siscontrol.mobile.core

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NfcTagReader {
    private val _tagCodes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val tagCodes = _tagCodes.asSharedFlow()

    fun publishFromIntent(intent: Intent?) {
        if (intent == null) return
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
        _tagCodes.tryEmit(tag.id.toHexString())
    }

    private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
        "%02X".format(byte)
    }
}
