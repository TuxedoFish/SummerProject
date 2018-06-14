package com.example.harry.umbrellafindr.utils;

import android.net.Uri;

public class User {
    private String firstName, email;
    private int age;
    private Uri imageURI;

    private enum Gender {
            MALE,
            Female
    }
    private Gender gender;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public Uri getImageURI() { return imageURI; }
    public void setImageURI(Uri imageURI) { this.imageURI = imageURI; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
}
