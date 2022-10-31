package com.example.apkdownload;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Api {
    @Multipart
    @POST("/uploadApk")
    Call<ResponseBody> postFile(@Part MultipartBody.Part fileData, @Query("filename") String apkName);
}
