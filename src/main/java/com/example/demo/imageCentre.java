package com.example.demo;

public class imageCentre {
    private final int x;
    private final int y;
    private final int rotation;
    //represent the x and y position of the image centre
    public imageCentre(int x,int y,int rotation){
        this.x=x;
        this.y=y;
        this.rotation=rotation;
    }
    //returns the x and y positions
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getRotation(){
        return rotation;
    }

}
