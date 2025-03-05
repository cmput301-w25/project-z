package com.example.z;

import android.content.Intent;
import android.widget.Toast;
import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;

public class LogInController {
    private Context context;
    private FirebaseAuth mAuth;

    public LogInController(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, ProfileActivity.class));
                        ((LogInActivity) context).finish(); // Close the LogInActivity
                    } else {
                        Toast.makeText(context, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
