package com.infbyte.amuzeo.repo

import java.security.MessageDigest

typealias ContentId = String

fun contentId(input: ByteArray): ContentId {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    return messageDigest.digest(input).toString()
}
