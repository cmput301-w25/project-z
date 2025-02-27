package com.example.myapplication;

public class EmotionalStates {
    public boolean angry = false;
    public boolean sad = false;
    public boolean happy = false;
    public boolean scared = false;
    public boolean confused = false;
    public boolean disgusted = false;
    public boolean surprised = false;
    public boolean ashamed = false;

    public String GetState() {
        if (angry) return "angry";
        if (sad) return "sad";
        if (happy) return "happy";
        if (scared) return "scared";
        if (confused) return "confused";
        if (disgusted) return "disgusted";
        if (surprised) return "surprised";
        if (ashamed) return "ashamed";
        return "Not set";
    }

    public void setState(String state) {
        switch (state.toLowerCase()) {
            case "angry": angry = true; break;
            case "sad": sad = true; break;
            case "happy": happy = true; break;
            case "scared": scared = true; break;
            case "confused": confused = true; break;
            case "disgusted": disgusted = true; break;
            case "surprised": surprised = true; break;
            case "ashamed": ashamed = true; break;
            default: System.out.println("Invalid state: " + state);
        }
    }
}
