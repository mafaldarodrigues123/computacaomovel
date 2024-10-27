package com.example.marsphotos.data

import com.example.marsphotos.model.PicsumPhoto
import com.example.marsphotos.network.PicsumApiService

interface PicsumPhotosRepository {
    /** Fetches list of PicsumPhoto from marsApi */
    suspend fun getPicsumPhotos(): List<PicsumPhoto>
}

/**
 * Network Implementation of Repository that fetch picsum photos list from picsumApi.
 */
class NetworkPicsumPhotosRepository(
    private val picsumApiService: PicsumApiService
) : PicsumPhotosRepository {
    /** Fetches list of PicsumPhoto from marsApi*/
    override suspend fun getPicsumPhotos(): List<PicsumPhoto> = picsumApiService.getPhotos()
}
