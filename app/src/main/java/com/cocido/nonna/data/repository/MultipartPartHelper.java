package com.cocido.nonna.data.repository;

import java.io.File;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Helper en Java para crear MultipartBody.Part sin ambigüedad de overloads
 * que provoca errores de compilación en Kotlin con OkHttp.
 */
public final class MultipartPartHelper {

    private MultipartPartHelper() {}

    public static MultipartBody.Part createFormDataFile(String partName, File file, MediaType mediaType) {
        RequestBody body = RequestBody.create(mediaType, file);
        String disposition = "form-data; name=\"" + partName + "\"; filename=\"" + file.getName() + "\"";
        Headers headers = new Headers.Builder().add("Content-Disposition", disposition).build();
        return MultipartBody.Part.create(headers, body);
    }
}
