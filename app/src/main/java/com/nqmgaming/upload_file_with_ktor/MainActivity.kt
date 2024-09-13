package com.nqmgaming.upload_file_with_ktor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nqmgaming.upload_file_with_ktor.ui.theme.UploadFileWithKtorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UploadFileWithKtorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = viewModel {
                        UploadFileViewModel(
                            repository = UploadFileRepository(
                                httpClient = HttpClient.client,
                                fileReader = FileReader(
                                    context = applicationContext
                                )
                            )
                        )
                    }

                    val state = viewModel.state

                    val filePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { contentUri ->
                        contentUri?.let {
                            viewModel.uploadFile(it)
                        }
                    }

                    val animatedProgress by animateFloatAsState(
                        targetValue = state.progress,
                        animationSpec = tween(durationMillis = 100),
                        label = "LinearProgressIndicator"
                    )

                    LaunchedEffect(key1 = state) {
                        when {
                            state.errorMessage != null -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    state.errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            state.isUploadComplete -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Upload complete",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            !state.isUploading && !state.isUploadComplete -> {
                                Button(
                                    onClick = {
                                        filePickerLauncher.launch("*/*")
                                    }
                                ) {
                                    Text("Pick a file")
                                }
                            }

                            state.isUploadComplete -> {
                                Column {
                                    Text("Upload complete")
                                    Button(
                                        onClick = {
                                            filePickerLauncher.launch("*/*")
                                        }
                                    ) {
                                        Text("Pick a file")
                                    }
                                }
                            }

                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                            .height(16.dp),
                                    )
                                    Text(
                                        text = "Uploading ${state.progress * 100}%",
                                        modifier = Modifier.padding(16.dp)
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.cancelUpload()
                                        }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

