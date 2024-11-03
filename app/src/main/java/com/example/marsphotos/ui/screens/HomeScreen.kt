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

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.room.Room
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.R
import com.example.marsphotos.data.room.AppDatabase
import com.example.marsphotos.data.room.ImageEntity
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.model.PicsumPhoto
import com.example.marsphotos.ui.createImageFile
import com.example.marsphotos.ui.theme.MarsPhotosTheme
import kotlinx.coroutines.launch
import java.util.Objects

@Composable
fun HomeScreen(
    marsUiState: MarsUiState,
    picsumUiState: PicsumUiState?,
    rollCount: Int,
    retryActionMars: () -> Unit,
    retryActionPicsum: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onRollClick: () -> Unit,
    onBlurClick: () -> Unit,
    onGrayClick: () -> Unit,
    onLoadClick: () -> Unit,
    onSaveClick: () -> Unit,
    onSaveCameraPic: (String) -> Unit,
) {
    val context = LocalContext.current
    val database = remember { getDatabase(context) }
    val imageDao = remember { database.imageDao() }
    val scope = rememberCoroutineScope()
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )
    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            capturedImageUri = uri
            scope.launch {
                imageDao.insertImage(ImageEntity(uri = capturedImageUri.toString()))
                onSaveCameraPic(capturedImageUri.toString())
            }
        }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        cameraLauncher.launch(uri)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .clipToBounds()
        ) {
            when {
                marsUiState is MarsUiState.Loading || picsumUiState is PicsumUiState.Loading -> {
                    LoadingScreen(modifier = modifier.fillMaxSize())
                }
                marsUiState is MarsUiState.Error -> {
                    ErrorScreen(retryAction = retryActionMars, modifier = modifier.fillMaxSize())
                }
                picsumUiState is PicsumUiState.Error -> {
                    ErrorScreen(retryAction = retryActionPicsum, modifier = modifier.fillMaxSize())
                }
                marsUiState is MarsUiState.Success && picsumUiState is PicsumUiState.Success -> {
                    ResultScreen(
                        marsString = marsUiState.phrase,
                        picsumString = picsumUiState.phrase,
                        marsPhoto = marsUiState.randomPhoto,
                        picsumPhoto = picsumUiState.randomPhoto,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(contentPadding)
                    )
                }
            }

            var image by remember { mutableStateOf<ImageEntity?>(null) }
            LaunchedEffect(capturedImageUri) {
                image = imageDao.getImage()
                Log.d("Database Load", "Loaded Image URI: ${image?.uri}")
            }

            Text("roll done $rollCount times", modifier = Modifier.padding(8.dp))

            image?.let {
                AsyncImage(
                    model = Uri.parse(it.uri),
                    contentDescription = "Saved Image",
                    modifier = Modifier.size(100.dp).clipToBounds()
                )
            }
        }




        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onRollClick) { Text("Roll") }
                Button(onClick = onBlurClick) { Text("Blur") }
                Button(onClick = onGrayClick) { Text("Gray") }
                Button(onClick = onLoadClick) { Text("Load") }
                Button(onClick = onSaveClick) { Text("Save") }
            }

            Button(onClick = {
                val permissionCheckResult =
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(uri)
                } else {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }) {
                Text(text = "Take Photo")
            }
        }
    }
}

fun getDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "image_database"
    ).build()
}

/**
 * The home screen displaying the loading message.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}

/**
 * The home screen displaying error message with re-attempt button.
 */
@Composable
fun ErrorScreen(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * The home screen displaying photo grid.
 */
@Composable
fun ResultScreen(
    marsString: String,
    picsumString: String,
    marsPhoto: MarsPhoto,
    picsumPhoto: PicsumPhoto,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = marsString)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(marsPhoto.imgSrc)
                .crossfade(true)
                .build(),
            contentDescription = "Photo of mars",
            contentScale = ContentScale.FillWidth
        )
        Text(picsumString)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(picsumPhoto.downloadUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Photo from picsum",
            contentScale = ContentScale.FillWidth
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    MarsPhotosTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    MarsPhotosTheme {
        ErrorScreen({})
    }
}

