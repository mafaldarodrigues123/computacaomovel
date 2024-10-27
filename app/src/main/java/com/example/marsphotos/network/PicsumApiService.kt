package com.example.marsphotos.network

import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.model.PicsumPhoto
import retrofit2.http.GET

/**
 * A public interface that exposes the [getPhotos] method
 */
interface PicsumApiService {
    /**
     * Returns a [List] of [PicsumPhoto] and this method can be called from a Coroutine.
     * The @GET annotation indicates that the "photos" endpoint will be requested with the GET
     * HTTP method
     */
    @GET("/v2/list")
    suspend fun getPhotos(): List<PicsumPhoto>
}
