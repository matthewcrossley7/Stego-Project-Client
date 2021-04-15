package com.example.demo;

public class imagePos {
    private int[] currPos;
    private char direction;
    private int currDistance;
    private int maxLength;
    public imagePos(int[] currPos,char direction,int currDistance, int maxLength){
        this.currPos=currPos;
        this.direction=direction;
        this.currDistance=currDistance;
        this.maxLength=maxLength;
    }
    public int[] getCurrPos(){
        return currPos;
    }
    public char getDirection(){
        return direction;
    }
    public int getCurrDistance(){
        return currDistance;
    }
    public int getMaxLength(){
        return maxLength;
    }
    public void setCurrPos(int[] currPos){
        this.currPos=currPos;
    }
    public void setDirection(char direction){
            this.direction=direction;
    }
    public void setCurrDistance(int currDistance){
        this.currDistance=currDistance;
    }
    public void setMaxLength(int maxLength){
        this.maxLength=maxLength;
    }
    public void incX(){
        currPos[0]++;
    }
    public void decX(){
        currPos[0]--;
    }
    public void incY(){
        currPos[1]++;
    }
    public void decY(){
        currPos[1]--;
    }
    public void incMaxLength(){
        this.maxLength++;
    }
    public void incCurrDist(){
        this.currDistance++;
    }
}
