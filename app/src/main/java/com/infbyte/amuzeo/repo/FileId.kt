package com.infbyte.amuzeo.repo

import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.fileSize

typealias ContentId = String

@OptIn(ExperimentalStdlibApi::class)
fun Path.fileId(): ContentId {
    val byteArray = "$this${fileSize()}".toByteArray()
    val messageDigest = MessageDigest.getInstance("SHA-256")
    return messageDigest.digest(byteArray).toHexString()
}
