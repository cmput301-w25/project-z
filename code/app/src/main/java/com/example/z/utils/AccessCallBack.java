package com.example.z.utils;

/**
 * AccessCallBack serves as callback interface used to handle asynchronous operations
 *
 *  Outstanding Issues:
 *      - None
 */
public interface AccessCallBack {
    void onAccessResult(boolean isSuccess, String message);
}

