package com.example.uploadimagetest.repository

import com.example.uploadimagetest.listener.ProgressListener
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class ProgressRequestBody(private val contentType: MediaType? = null
                          , private val file: File
                          , private val listener: ProgressListener
) : RequestBody() {



    override fun contentType(): MediaType? =contentType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {

        val inputStream = FileInputStream(file)
        val buffer = ByteArray(BUFFER_SIZE)
        var uploaded: Long = 0
        val fileSize = file.length()

        try {
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) break

                uploaded += read
                sink.write(buffer, 0, read)

                val progress = (((uploaded / fileSize.toDouble())) * 100).toInt()
                listener.onProgress( progress )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream.close()
        }
    }
    companion object {
        const val BUFFER_SIZE = 2048
    }

}