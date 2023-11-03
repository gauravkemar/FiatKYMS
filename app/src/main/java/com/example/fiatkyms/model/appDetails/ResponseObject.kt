package com.example.fiatkyms.model.appDetails

data class ResponseObject(
    val apkFileUrl: String,
    val apkVersion: Int,
    val apkVersionDisplayString: String,
    val isMandatory: Boolean
)