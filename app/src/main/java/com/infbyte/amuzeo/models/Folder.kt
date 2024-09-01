package com.infbyte.amuzeo.models

data class Folder(val name: String, val numberOfVideos: Int) {
    companion object {
        val DEFAULT = Folder("Folder", 0)
    }
}
