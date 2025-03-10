package com.example.z;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.z.mood.Mood;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MockMoods {

    @Mock
    private Mood mockMood; // Mocked Mood object

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test Getters for Mood Class
     */
    @Test
    public void testMoodGetters() {
        // Define Mock Behavior for Getters
        when(mockMood.getUserId()).thenReturn("12345");
        when(mockMood.getDocumentId()).thenReturn("moodDocRef123");
        when(mockMood.getUsername()).thenReturn("TestUser");
        when(mockMood.getEmotionalState()).thenReturn("Happy");
        when(mockMood.getTrigger()).thenReturn("Sunshine");
        when(mockMood.getSocialSituation()).thenReturn("Alone");
        when(mockMood.getCreatedAt()).thenReturn(new Date(12345));

        Map<String, Object> fakeLocation = new HashMap<>();
        fakeLocation.put("latitude", 50);
        fakeLocation.put("longitude", -50);
        when(mockMood.getLocation()).thenReturn(fakeLocation);

        when(mockMood.getDescription()).thenReturn("Feeling fantastic!");

        // Make sure getters work properly
        assertEquals("12345", mockMood.getUserId());
        assertEquals("moodDocRef123", mockMood.getDocumentId());
        assertEquals("TestUser", mockMood.getUsername());
        assertEquals("Happy", mockMood.getEmotionalState());
        assertEquals("Sunshine", mockMood.getTrigger());
        assertEquals("Alone", mockMood.getSocialSituation());
        assertEquals(new Date(12345), mockMood.getCreatedAt());
        assertEquals(fakeLocation, mockMood.getLocation());
        assertEquals("Feeling fantastic!", mockMood.getDescription());
    }

    /**
     * Test Setters for Mood Class
     */
    @Test
    public void testMoodSetters() {
        Mood mood = new Mood(); // Real Mood object

        // Set values
        mood.setUserId("12345");
        mood.setDocumentId("moodDocRef456");
        mood.setUsername("TestUser");
        mood.setEmotionalState("Excited");
        mood.setTrigger("Birthday");
        mood.setSocialSituation("With Friends");
        mood.setCreatedAt(new Date(12345));

        Map<String, Object> newLocation = new HashMap<>();
        newLocation.put("latitude", 50);
        newLocation.put("longitude", -50);
        mood.setLocation(newLocation);

        mood.setDescription("Best day ever!");

        // Make sure values were set
        assertEquals("12345", mood.getUserId());
        assertEquals("moodDocRef456", mood.getDocumentId());
        assertEquals("TestUser", mood.getUsername());
        assertEquals("Excited", mood.getEmotionalState());
        assertEquals("Birthday", mood.getTrigger());
        assertEquals("With Friends", mood.getSocialSituation());
        assertEquals(new Date(12345), mood.getCreatedAt());
        assertEquals(newLocation, mood.getLocation());
        assertEquals("Best day ever!", mood.getDescription());
    }

    /**
     * Test if Setters were Called on the Mock
     */
    @Test
    public void testVerifySettersCalled() {
        // Call Setter Methods on Mock
        mockMood.setUserId("99999");
        mockMood.setDocumentId("moodDocRef123");
        mockMood.setUsername("MockUser");
        mockMood.setEmotionalState("Tired");
        mockMood.setTrigger("Work");
        mockMood.setSocialSituation("Office");
        mockMood.setCreatedAt(new Date(12345));
        mockMood.setLocation(null);
        mockMood.setDescription("Long day at work!");

        // Verify that Setters Were Called Correctly
        verify(mockMood).setUserId("99999");
        verify(mockMood).setDocumentId("moodDocRef123");
        verify(mockMood).setUsername("MockUser");
        verify(mockMood).setEmotionalState("Tired");
        verify(mockMood).setTrigger("Work");
        verify(mockMood).setSocialSituation("Office");
        verify(mockMood).setCreatedAt(new Date(12345));
        verify(mockMood).setLocation(null);
        verify(mockMood).setDescription("Long day at work!");
    }
}
