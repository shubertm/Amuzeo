package com.infbyte.amuzeo.di

import com.infbyte.amuzeo.playback.AmuzeoPlayer
import com.infbyte.amuzeo.playback.AmuzeoPlayerImpl
import org.koin.dsl.module

val playbackModule =
    module {
        single<AmuzeoPlayer> { AmuzeoPlayerImpl(get()) }
    }
