package com.nqmgaming.upload_file_with_ktor

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class FileReader(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun uriToFileInfo(contentURi: Uri): FileInfo {
        return withContext(ioDispatcher) {
            val byte = context.contentResolver.openInputStream(contentURi)?.use { inputStream ->
                inputStream.readBytes()
            }
            val fileName = contentURi.path?.substringAfterLast("/") ?: UUID.randomUUID().toString()
            val minType = context.contentResolver.getType(contentURi) ?: "application/octet-stream"
            FileInfo(
                name = fileName,
                minType = minType,
                bytes = byte ?: byteArrayOf()
            )
        }
    }
}

class FileInfo(
    val name: String,
    val minType: String,
    val bytes: ByteArray
)