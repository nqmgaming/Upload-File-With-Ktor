package com.nqmgaming.upload_file_with_ktor

import android.net.Uri
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class UploadFileRepository (
    private val httpClient: HttpClient,
    private val fileReader: FileReader
){
    fun uploadFile(contentUri: Uri) : Flow<ProgressUpdate> = channelFlow {
        val info = fileReader.uriToFileInfo(contentUri)

        httpClient.submitFormWithBinaryData(
            url = "https://dlptest.com/https-post/",
            formData = formData {
                append("description", "{test}")
                append("the_file", info.bytes, Headers.build {
                    append(HttpHeaders.ContentType, info.minType)
                    append(HttpHeaders.ContentDisposition, "filename=${info.name}")

                })
            }
        ) {
            onUpload { bytesSentTotal, contentLength ->
                send(ProgressUpdate(bytesSentTotal, contentLength))
            }
        }
    }
}

data class ProgressUpdate(
    val bytesSentTotal: Long,
    val contentLength: Long,
    val errorMessage: String? = null
)