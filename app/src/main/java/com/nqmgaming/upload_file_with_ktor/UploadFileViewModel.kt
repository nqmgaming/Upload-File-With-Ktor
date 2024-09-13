package com.nqmgaming.upload_file_with_ktor

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import okio.FileNotFoundException

class UploadFileViewModel(
    private val repository: UploadFileRepository
) : ViewModel() {
    var state by mutableStateOf(UpdateState())
        private set

    private var uploadFile: Job? = null

    fun uploadFile(contentUri: Uri) {
        uploadFile = repository.uploadFile(contentUri)
            .onStart {
                state = state.copy(
                    isUploading = true,
                    isUploadComplete = false,
                    progress = 0f,
                    errorMessage = null
                )
            }.onEach { progressUpdate ->
                state = state.copy(
                    progress = progressUpdate.bytesSentTotal.toFloat() / progressUpdate.contentLength
                )
            }.onCompletion { cause ->
                state = if (cause is CancellationException) {
                    state.copy(
                        isUploading = false,
                        isUploadComplete = false,
                        errorMessage = cause.message
                    )
                } else {
                    state.copy(
                        isUploading = false,
                        isUploadComplete = true
                    )
                }
            }
            .catch { cause ->
                val message = when (cause) {
                    is OutOfMemoryError -> "Out of memory"
                    is FileNotFoundException -> "File not found"
                    is UnresolvedAddressException -> "Unresolved address"
                    else -> {
                        cause.message ?: "Something went wrong"
                    }
                }
                state = state.copy(
                    isUploading = false,
                    isUploadComplete = false,
                    errorMessage = message
                )
            }
            .launchIn(viewModelScope)
    }

    fun cancelUpload() {
        uploadFile?.cancel()
    }
}

data class UpdateState(
    val isUploading: Boolean = false,
    val isUploadComplete: Boolean = false,
    val progress: Float = 0f,
    val errorMessage: String? = null
)