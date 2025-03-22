package com.example.z.utils;

import com.example.z.user.User;
import java.util.List;

public interface OnUserSearchCompleteListener {
    void onSuccess(List<User> users);
    void onFailure(Exception e);
}