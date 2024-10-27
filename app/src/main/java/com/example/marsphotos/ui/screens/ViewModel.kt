/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.MarsPhotosRepository
import com.example.marsphotos.data.PicsumPhotosRepository
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.model.PicsumPhoto
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * UI state for the Home screen
 */
sealed interface MarsUiState {
    data class Success(val phrase: String, val randomPhoto: MarsPhoto) : MarsUiState
    object Error : MarsUiState
    object Loading : MarsUiState
}

sealed interface PicsumUiState {
    data class Success(val phrase: String, val randomPhoto: PicsumPhoto) : PicsumUiState
    object Error : PicsumUiState
    object Loading : PicsumUiState
}

class ViewModel(private val marsPhotosRepository: MarsPhotosRepository,
                private val picsumPhotosRepository: PicsumPhotosRepository
) : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    var picsumUiState: PicsumUiState by mutableStateOf(PicsumUiState.Loading)
        private set

    /**
     * Call getMarsPhotos() and getPicsumPhotos() on init so we can display status immediately.
     */
    init {
        getMarsPhotos()
        getPicsumPhotos()
    }

    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     * [MarsPhoto] [List] [MutableList].
     */
    fun getMarsPhotos() {
        viewModelScope.launch {
            marsUiState = MarsUiState.Loading
            marsUiState = try {
                val list = marsPhotosRepository.getMarsPhotos()
                MarsUiState.Success(
                    "${list.size} Mars photos retrieved", list.random()
                )
            } catch (e: IOException) {
                MarsUiState.Error
            } catch (e: HttpException) {
                MarsUiState.Error
            }
        }
    }

    fun getPicsumPhotos(){
        viewModelScope.launch {
            picsumUiState = try {
                val list = picsumPhotosRepository.getPicsumPhotos()
                PicsumUiState.Success(
                    "${list.size} picsum photos retrieved", list.random()
                )
            } catch (e: IOException) {
                PicsumUiState.Error
            } catch (e: HttpException) {
                PicsumUiState.Error
            }
        }
    }

    fun applyBlur() {
        if (picsumUiState is PicsumUiState.Success) {
            val currentPhoto = (picsumUiState as PicsumUiState.Success).randomPhoto
            val blurredPhoto = currentPhoto.copy(downloadUrl = "${currentPhoto.downloadUrl}?blur")
            picsumUiState = PicsumUiState.Success(
                phrase = (picsumUiState as PicsumUiState.Success).phrase,
                randomPhoto = blurredPhoto
            )
        }
    }

    fun applyGray() {
        if (picsumUiState is PicsumUiState.Success) {
            val currentPhoto = (picsumUiState as PicsumUiState.Success).randomPhoto
            val grayPhoto = currentPhoto.copy(downloadUrl = "${currentPhoto.downloadUrl}?grayscale")
            picsumUiState = PicsumUiState.Success(
                phrase = (picsumUiState as PicsumUiState.Success).phrase,
                randomPhoto = grayPhoto
            )
        }
    }

    /**
     * Factory for [ViewModel] that takes [MarsPhotosRepository] as a dependency
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                val marsPhotosRepository = application.container.marsPhotosRepository
                val picsumPhotosRepository = application.container.picsumPhotoRepository
                ViewModel(marsPhotosRepository = marsPhotosRepository, picsumPhotosRepository = picsumPhotosRepository)
            }
        }
    }
}
