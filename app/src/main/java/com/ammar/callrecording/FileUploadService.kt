package com.ammar.callrecording

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileUploadService {
    @Multipart
    @POST("api/Audio")
    fun uploadAudioFile(
//        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>
}
