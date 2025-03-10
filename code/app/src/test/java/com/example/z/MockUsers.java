package com.example.z;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.z.user.User;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MockUsers {

    @Mock
    private User mockUser; // Mocked User object

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize Mockito annotations
    }

    /**
     * Test Getters for User Class
     */
    @Test
    public void testUserGetters() {
        // Define Mock Behavior for Getters
        when(mockUser.getEmail()).thenReturn("testuser@example.com");
        when(mockUser.getUsername()).thenReturn("TestUser");

        // Make sure getters retrieve user details
        assertEquals("testuser@example.com", mockUser.getEmail());
        assertEquals("TestUser", mockUser.getUsername());
    }

    /**
     * Test Setters for User Class
     */
    @Test
    public void testUserSetters() {
        User user = new User(); // Real User object

        // Set values
        user.setEmail("anotheruser@example.com");
        user.setUsername("AnotherUser");

        // Make sure setters work properly
        assertEquals("anotheruser@example.com", user.getEmail());
        assertEquals("AnotherUser", user.getUsername());
    }

    /**
     * Test if Setters were Called on the Mock
     */
    @Test
    public void testVerifySettersCalled() {
        // Call Setter Methods on Mock
        mockUser.setEmail("mockuser@example.com");
        mockUser.setUsername("MockUser");

        // Make sure the user properly set their info
        verify(mockUser).setEmail("mockuser@example.com");
        verify(mockUser).setUsername("MockUser");
    }
}

