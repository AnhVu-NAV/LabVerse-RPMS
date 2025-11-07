package com.prm392.g5.labverse.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.prm392.g5.labverse.config.RetrofitClient;
import com.prm392.g5.labverse.dto.ErrorResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class ApiErrorHandler {

    private ApiErrorHandler() {
        // Private constructor to prevent instantiation
    }

    /**
     * called when can send the request but server return error response
     * có thể gửi request nhưng server trả về lỗi
     * @param context the context to show Toast
     * @param response the retrofit response
     * @param tag the log tag
     * @param <T> the response body type
     */
    public static <T> void handleApiResponseError(Context context, Response<T> response, String tag) {
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody == null) {
                Log.e(tag, "Empty error body");
                Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
                return;
            }

            Converter<ResponseBody, ErrorResponse> converter =
                    RetrofitClient.getInstance()
                            .responseBodyConverter(ErrorResponse.class, new Annotation[0]);
            ErrorResponse errorResponse = converter.convert(errorBody);

            if (errorResponse == null) {
                throw new IOException("ErrorResponse is null");
            }

            int code = errorResponse.getCode();
            String message = errorResponse.getMessage();

            Log.e(tag, "Error " + code + ": " + message);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(tag, "Failed to parse error response", e);
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * called when can not send the request
     * không thể gửi request về server backend
     * @param context the context to show Toast
     * @param t the throwable error
     * @param tag the log tag
     */
    public static void handleNetworkFailure(Context context, Throwable t, String tag) {
        Log.e(tag, "Request failed", t);

        if (t instanceof java.net.UnknownHostException) {
            Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show();
        } else if (t instanceof java.net.SocketTimeoutException) {
            Toast.makeText(context, "Timeout!", Toast.LENGTH_SHORT).show();
        } else if (t instanceof java.net.ConnectException) {
            Toast.makeText(context, "Unable to connect to server", Toast.LENGTH_SHORT).show();
        } else if (t instanceof javax.net.ssl.SSLException) {
            Toast.makeText(context, "SSL Exception", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
        }
    }
}
