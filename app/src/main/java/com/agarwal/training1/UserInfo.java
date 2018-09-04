package com.agarwal.training1;

import android.net.Uri;

public class UserInfo {
    private  String Name;
    private  String Email;
    private  String Gender;
    private  String DOB;
    private  String UUid;

    public UserInfo(String name, String email, String gender, String dob, String uid) {
        Name = name;
        Email = email;
        Gender = gender;
        DOB = dob;
        UUid = uid;
    }

    public  UserInfo(){

    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getDob() {
        return DOB;
    }

    public void setDob(String dob) {
        DOB = dob;
    }

    public String getUuid() {
        return UUid;
    }

    public void setUuid(String uid) {
        UUid = uid;
    }
}
