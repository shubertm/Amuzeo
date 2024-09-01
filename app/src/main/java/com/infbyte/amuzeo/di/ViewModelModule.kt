package com.infbyte.amuzeo.di

import com.infbyte.amuzeo.presentation.viewmodels.VideosViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModelOf(::VideosViewModel)
    }
