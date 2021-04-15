package com.example.demo;

public class imageMsg {
    public String fromEmail;
    public String toEmail;
    public String date;
    public String image;
    public int imageID;
    public imageMsg(String fromEmail,String date,String image,String toEmail,int imageID){
        this.fromEmail=fromEmail;
        this.toEmail=toEmail;
        this.date=date;
        this.image=image;
        this.imageID=imageID;
    }

}
