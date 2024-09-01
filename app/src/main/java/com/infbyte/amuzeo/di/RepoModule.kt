package com.infbyte.amuzeo.di

import com.infbyte.amuzeo.repo.VideosRepo
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repoModule =
    module {
        singleOf(::VideosRepo)
    }
