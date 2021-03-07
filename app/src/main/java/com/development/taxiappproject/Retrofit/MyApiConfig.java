package com.development.taxiappproject.Retrofit;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface MyApiConfig {

//    @Multipart
//    @POST("/upload")
//    Call<ServerResponse> uploadFile(
//            @Header("token") String userToken,
//            @Part MultipartBody.Part file,
//            @Part("category") RequestBody documents,
//            @Part("token") RequestBody token
//    );

    @Multipart
    @POST("user/updateprofile")
    Observable<ResponseBody> updateProfile(
            @Part("user_id") RequestBody id,
            @Part("full_name") RequestBody fullName,
            @Part MultipartBody.Part image,
            @Part("other") RequestBody other
    );

    @Multipart
    @POST("upload")
    Observable<Response<ResponseBody>> uploadSingleImage(
            @Header("token") String userToken,
            @Part MultipartBody.Part file,
            @Part("category") RequestBody documents,
            @Part("token") RequestBody token,
            @Part("permission") RequestBody permission
    );

    @POST("upload/multi")
    Observable<Response<ResponseBody>> uploadMultiImage(@Body RequestBody file);

//    @Multipart
//    @POST("upload/multi")
//    Observable<Response<ResponseBody>> uploadMultiImage(
//            @Header("token") String userToken,
//            @Part("uploadFiles") MultipartBody.Part[] multiImage,
//            @Part("category") RequestBody document
//    );

//    @POST("/upload_multi_files/MultiPartUpload.php")
//    Call<ResponseBody> uploadMultiFile(@Body RequestBody file);

//    @Multipart
//    @POST("upload")
//    Observable<Response<ResponseBody>> uploadMultiImages(
//            @Header("token") String userToken,
//            @Part MultipartBody.Part file,
//            @Part("category") RequestBody documents,
//            @Part("token") RequestBody token,
//            @Part("permission") RequestBody permission
//    );

//    @Multipart
//    @POST("retrofit_example/upload_multiple_files.php")
//    Call<ServerResponse> uploadMulFile(@Part MultipartBody.Part file1, @Part MultipartBody.Part file2);
//
//    @GET("/rides/cars")
//    Call<ServerResponse> getCarList(
//            @Header("token") String userToken
//    );

    //*********************************************

//    @POST('user')
//    Call<User> createAccount(@Body User user);

//    @Multipart
//    @POST("upload")
//    Call<ResponseBody> uploadPhoto(
//            @Part("description") RequestBody description,
//            @Part MultipartBody.Part photo);
}
