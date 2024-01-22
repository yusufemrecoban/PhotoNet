package com.example.afinal.model;

public class UserModel {

    String name;
    String email;

    public UserModel(){
    }

    public UserModel(String name,String email){
        this.name=name;
        this.email=email;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email=email;
    }
}
