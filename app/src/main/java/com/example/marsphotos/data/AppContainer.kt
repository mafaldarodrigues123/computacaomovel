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
package com.example.marsphotos.data

import com.example.marsphotos.network.MarsApiService
import com.example.marsphotos.network.PicsumApiService
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val marsPhotosRepository: MarsPhotosRepository
    val picsumPhotoRepository: PicsumPhotosRepository
}

/**
 * Implementation for the Dependency Injection container at the application level.
 *
 * Variables are initialized lazily and the same instance is shared across the whole app.
 */
class DefaultAppContainer : AppContainer {
    private val baseUrlMars = "https://android-kotlin-fun-mars-server.appspot.com/"
    private val baseUrlPicsum = "https://picsum.photos/"

    /**
     * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
     */
    private val retrofitMars: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrlMars)
        .build()

    private val retrofitPicsum: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrlPicsum)
        .build()

    /**
     * Retrofit service object for creating api calls
     */

    private val retrofitServiceMars: MarsApiService by lazy {
        retrofitMars.create(MarsApiService::class.java)
    }

    private val retrofitServicePicsum: PicsumApiService by lazy {
        retrofitPicsum.create(PicsumApiService::class.java)
    }

    /**
     * DI implementation for Mars photos repository
     */
    override val marsPhotosRepository: MarsPhotosRepository by lazy {
        NetworkMarsPhotosRepository(retrofitServiceMars)
    }

    override val picsumPhotoRepository: PicsumPhotosRepository by lazy {
        NetworkPicsumPhotosRepository(retrofitServicePicsum)
    }
}
