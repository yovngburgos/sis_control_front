package com.siscontrol.mobile.data.remote.dto

data class NfcScanRequest(
    val executionId: Long,
    val nfcTagCode: String
)
