package com.example.z;

import static org.mockito.Mockito.*;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;

import com.example.z.views.LogInActivity;
import com.example.z.views.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FirebaseAuth.class })
public class MainActivityTest {

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private FirebaseFirestore mockFirestore;

    private MainActivity mainActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mainActivity = new MainActivity();

        // Mock Firebase authentication to return our mockAuth instance
        PowerMockito.mockStatic(FirebaseAuth.class);
        when(FirebaseAuth.getInstance()).thenReturn(mockAuth);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
    }

    @Test
    public void testRedirectToProfileActivity_WhenUserLoggedIn() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        Intent expectedIntent = new Intent(ApplicationProvider.getApplicationContext(), ProfileActivity.class);
        mainActivity.startActivity(expectedIntent);

        verify(mockAuth, times(1)).getCurrentUser();
    }

    @Test
    public void testRedirectToLogInActivity_WhenUserNotLoggedIn() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        Intent expectedIntent = new Intent(ApplicationProvider.getApplicationContext(), LogInActivity.class);
        mainActivity.startActivity(expectedIntent);

        verify(mockAuth, times(1)).getCurrentUser();
    }
}
