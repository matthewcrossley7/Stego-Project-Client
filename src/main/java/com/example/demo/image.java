package com.example.demo;

import net.sf.image4j.util.ConvertUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;


public class image {

    private String name;
    private int length;
    private BufferedImage img;
    private int height;
    private int width;
    private int middleBlockSize=3;
    private int bitDepth;
    private int separation;
    public image(String name) {
        this.name=name;
    }
    public image() {

    }
    //rotate the image in order
    public void rotateCw( )
    {
        int newWidth  = img.getWidth();
        int newHeight = img.getHeight();
        BufferedImage   newImage = new BufferedImage( newHeight, newWidth, img.getType() );
        //iterate over each pixel of image and set pixel values of newly created image
        for( int i=0 ; i < newWidth ; i++ )
            for( int j=0 ; j < newHeight ; j++ )
                newImage.setRGB( newHeight-1-j, i, img.getRGB(i,j) );
        img=newImage;
        height=img.getHeight();
        width=img.getWidth();
    }
    public static byte[] decodeImage(String imageDataString) {
        return Base64.getDecoder().decode(imageDataString);
    }
    //
    public void setString(String imageString) throws IOException {
        byte[] imageByteArray = decodeImage(imageString);
        InputStream is = new ByteArrayInputStream(imageByteArray);
        img = ImageIO.read(is);
        height=img.getHeight();
        width=img.getWidth();
    }
    //opens specified file so pixel values can be read
    public void readImage() throws IOException {
         File file= new File(name);
        //taken from https://www.tutorialspoint.com/how-to-get-pixels-rgb-values-of-an-image-using-java-opencv-library#:~:text=Retrieving%20the%20pixel%20contents%20(ARGB%20values)%20of%20an%20image%20%E2%88%92&text=Get%20the%20pixel%20value%20at,and%20getBlue()%20methods%20respectively.
        img = ImageIO.read(file);
        img = ConvertUtil.convert24(img);  //image converted to 24 bit colour depth
        saveNewImage("C:\\Users\\mcrossley\\Desktop\\images\\shitimage2.png");
        height=img.getHeight();            //set the height and width of image
        width=img.getWidth();
    }

    //read in the pixel values of the image
    public void readPixels() throws IOException {
        int pixel,red,green,blue;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //Retrieving contents of a pixel
                pixel = img.getRGB(x,y);
                //Creating a Color object from pixel value
                Color color = new Color(pixel, true);
                //Retrieving the R G B values
                red = color.getRed();
                green = color.getGreen();
                blue = color.getBlue();
                pixel =  ((red)<<16) | (green<<8) | blue;
                img.setRGB(x, y, pixel);
            }
        }
    }
    //returns the maximum separation between pixels that can be used to hide all contents of message
    public int getMaxSeparation(int msgLength,int bitDepth){
        int separation = (int)(Math.pow(Math.min(height, width),2)-20)/((3*msgLength)/bitDepth-1)-1;
        if(separation>=4096){   //if value is more than the maximum value it is set to 4095
            separation=4095;
        }
        return separation;
    }
    //hides the metadata of the message and creates the sequence of bits that represent the image centre
    public void hideMetaData(int length,int bitDepth,int separation) {
        this.separation=separation;
        this.bitDepth=bitDepth;
        int pixel, newPixel;
        String asBinary;
        Color color;
        int[] topLeft;
        int[] currPos;
        //finds the location of the centre depending on image dimensions
        if(width%2==0 && height%2==0){
            topLeft = new int[]{(width/2)-1,(height/2)-2};
        }else if(width%2==0 && height%2!=0){
            topLeft = new int[]{(width/2)-2,(height/2)-1};
        }else{
            topLeft = new int[]{(width/2)-1,(height/2)-1};
        }
        currPos = new int[]{topLeft[0]-1,topLeft[1]};
        //initialise spiral structure
        imagePos imagePos = new imagePos(currPos,'S',0,3);
        //iterate over the centre 9 pixels
        for(int x=0;x<3;x++){
            for(int y=0;y<3;y++){
                //if pixel is the top left of centre change LSBs to '010'
                if(x==0 &&y ==0){
                    pixel = img.getRGB(topLeft[0], topLeft[1]);
                    color = new Color(pixel, true);
                    newPixel = getNewColour(color,"010",1);
                    img.setRGB( topLeft[0],topLeft[1],newPixel);
                }else{
                    //if pixel is not top left of centre change LSBs to '101'
                    pixel = img.getRGB(topLeft[0]+x, topLeft[1]+y);
                    color = new Color(pixel, true);
                    newPixel = getNewColour(color,"101",1);
                    img.setRGB(topLeft[0]+x,topLeft[1]+y, newPixel);
                }
            }
        }
        //convert the length of image to binary
        String binary = Integer.toString(length,2);
        StringBuilder sb = new StringBuilder(binary);
        //pad length of message in binary with 0s so length is 15
        for(int z=0;z<15-binary.length();z++){
            sb.insert(0,'0');
        }
        binary=sb.toString();
        //loop over 5 pixels of image
        for(int x = 0;x<5;x++){
            //consider only 3 bits of the binary
            asBinary=binary.substring(x*3,x*3+3);
            currPos = imagePos.getCurrPos();
            //retrieve current pixel value of position
            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            //calculate the new pixel value after embedding bits at bit depth 1
            newPixel = getNewColour(color,asBinary,1);
            img.setRGB(currPos[0], currPos[1], newPixel);
            //find the next position of pixel in spiral configuration
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }
        //get binary value of number of LSBs being used decremented
        binary = Integer.toString(bitDepth-1,2);
        sb = new StringBuilder(binary);
        //pad value with 0s so length is 3
        for(int z=0;z<3-binary.length();z++){
            sb.insert(0,'0');
        }
        //get current position in spiral diagram
        currPos = imagePos.getCurrPos();
        pixel = img.getRGB(currPos[0], currPos[1]);
        color = new Color(pixel, true);
        //find new pixel value after embedding bits at bit depth 1 and set to new pixel value
        newPixel = getNewColour(color,sb.toString(),1);
        img.setRGB(currPos[0], currPos[1], newPixel);
        //find the next position of pixel in spiral configuration
        imagePos.incCurrDist();
        getNextPos(imagePos);
        //get binary value of separation between pixels being used
        binary = Integer.toString(separation,2);
        sb = new StringBuilder(binary);
        //pad binary value with 0s so length is 12
        for(int z=0;z<12-binary.length();z++){
            sb.insert(0,'0');
        }
        for(int x = 0;x<4;x++){
            //consider only 3 bits of the binary
            asBinary=sb.toString().substring(x*3,x*3+3);
            currPos = imagePos.getCurrPos();
            //find new pixel value after embedding bits at bit depth 1 and set to new pixel value
            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            newPixel = getNewColour(color,asBinary,1);
            img.setRGB(currPos[0], currPos[1], newPixel);
            //find the next position of pixel in spiral configuration
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }
    return;
    }
    //returns length of message and retrieves number of LSBs used and separation between pixels
    public int getMsgLength(imageCentre centre) {
        int pixel,red,green,blue;
        StringBuilder str  = new StringBuilder();
        StringBuilder str2  = new StringBuilder();
        StringBuilder str3  = new StringBuilder();
        String asBinary;
        Color color;
        int[] topLeft = new int[2];
        //sets the position of the top left pixel of the centre
        topLeft[0]=centre.getX();
        topLeft[1]=centre.getY();
        int[] currPos = new int[]{topLeft[0]-1,topLeft[1]};

        imagePos imagePos = new imagePos(currPos,'S',0,3);
        //iterate over 5 pixels of the spiral
        for(int x = 0;x<5;x++){
            //get current position
            currPos = imagePos.getCurrPos();
            //get current pixel values
            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);

            //append the least significant bits of the pixel colour values to the string builder
            str.append(getBits(color));
            //move along the spiral structure by 1 position
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }
        length = Integer.parseInt(str.toString(), 2);

        //obtain the number of LSBs being used by viewing LSBs of next pixel in spiral
        currPos = imagePos.getCurrPos();
        pixel = img.getRGB(currPos[0], currPos[1]);
        color = new Color(pixel, true);
        //append the least significant bits of the pixel colour values to the string builder
        str2.append(getBits(color));
        //number of LSBs used is found by adding 1 to the integer value of binary representation
        bitDepth= Integer.parseInt(str2.toString(),2)+1;

        //iterate over the next 4 pixels of the spiral
        for(int i=0;i<4;i++){
            //get the next position of spiral
            imagePos.incCurrDist();
            getNextPos(imagePos);
            currPos = imagePos.getCurrPos();
            //get the colour of the current pixel
            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            //append the least significant bits of the pixel colour values to the string builder
            str3.append(getBits(color));
        }
        //convert the binary value to an integer
        separation= Integer.parseInt(str3.toString(),2);
        return length;

    }
    //function returns the LSBs of a particular colour
    public String getBits(Color color){
        StringBuilder str = new StringBuilder();
        String asBinary;
        int red,green,blue;
        red = color.getRed();
        asBinary = Integer.toString(red,2);
        //add least significant bit of red component to string builder
        str.append(asBinary.charAt(asBinary.length()-1));
        green = color.getGreen();
        asBinary = Integer.toString(green,2);
        //add least significant bit of green component to string builder
        str.append(asBinary.charAt(asBinary.length()-1));
        blue = color.getBlue();
        asBinary = Integer.toString(blue,2);
        //add least significant bit of blue component to string builder
        str.append(asBinary.charAt(asBinary.length()-1));
        return str.toString();
    }

    //saves the created image at specified location
    public void saveNewImage(String fileName) throws IOException {
        File outPutImage = new File(fileName);
        ImageIO.write(img, "png", outPutImage);

    }
    //finds the centre of a stego-image by searching for sequence of bits and rotating if none found
    public imageCentre isCentre(){
        imageCentre imgCentre;
        imgCentre = findCentre();
        if(imgCentre!=null){
            return imgCentre;
        }
        rotateCw();
        imgCentre = findCentre();
        if(imgCentre!=null){
            return imgCentre;
        }
        rotateCw();
        imgCentre = findCentre();
        if(imgCentre!=null){
            return imgCentre;
        }
        rotateCw();
        imgCentre = findCentre();
        if(imgCentre!=null){
            return imgCentre;
        }
        //if no centre found then null returned
        return null;
    }
    //attempts to locate the sequence of bits representing the centre of an image. Returns position of top left pixel of centre 9 pixels
    public imageCentre findCentre(){
        int pixel,red,green,blue,countx,county;
        boolean valid;
        String redAsBinary,greenAsBinary,blueAsBinary;
        //iterates over all but the final 3 columns and 3 rows
        for(int x=0;x<width-middleBlockSize;x++) {
            for(int y=0;y<height-middleBlockSize;y++) {
                countx=0;
                county=0;
                valid = true;
                do{
                    //get colour components of pixel currently being observed
                    pixel = img.getRGB(x+countx, y+county);
                    Color color = new Color(pixel, true);
                    red = color.getRed();
                    redAsBinary = Integer.toString(red,2);
                    green = color.getGreen();
                    greenAsBinary = Integer.toString(green,2);
                    blue = color.getBlue();
                    blueAsBinary = Integer.toString(blue,2);
                    if(countx==0 && county==0) {
                        //compares LSBs of a pixel to values depending on location of the pixel
                        if(redAsBinary.charAt(redAsBinary.length()-1)!='0' || greenAsBinary.charAt(greenAsBinary.length()-1)!='1' ||blueAsBinary.charAt(blueAsBinary.length()-1)!='0'){
                            valid = false;
                        }
                    }else{
                        if(redAsBinary.charAt(redAsBinary.length()-1)!='1' || greenAsBinary.charAt(greenAsBinary.length()-1)!='0' ||blueAsBinary.charAt(blueAsBinary.length()-1)!='1'){
                            valid = false;
                        }
                    }
                    if(county>=middleBlockSize){
                        valid = false;
                    }
                     if(countx==middleBlockSize-1 &&  county==middleBlockSize-1 && valid){
                         county++;
                         //if all LSBs are 9 pixels observed match sequence then return image centre
                        return new imageCentre(x,y,0);
                    }else if(countx<middleBlockSize-1){
                        countx++;
                    }else{
                        countx=0;
                        county++;
                    }
                     //if LSBs do not match of sequence move to next pixel location
                }while(valid);
            }
        }
        //System.out.println("no center found");
        return null;
    }
    //returns new pixel value after a specific bit is replaces a bit at a certain depth
    public int getEmbeddedValue(int colour,char bit,int depth) {
                String asBinary = Integer.toString(colour,2);
        StringBuilder sb = new StringBuilder(asBinary);
        for(int z=0;z<8-asBinary.length();z++){
            sb.insert(0,'0');
        }
        asBinary=sb.toString();
         String newBinary = asBinary.substring(0, asBinary.length()-depth)+bit;
        if(depth>1){
             newBinary=newBinary+asBinary.substring(asBinary.length()-depth+1,asBinary.length());
        }
        return Integer.parseInt(newBinary, 2);
    }
    //returns new pixel colour after a binary string is embedded at a certain bit depth
    public int getNewColour(Color color, String binary,int depth){
             int blue,green,red,newBlue,newGreen,newRed;
             red = color.getRed();
             //hides first bit in red component
             newRed = getEmbeddedValue(red,binary.charAt(0),depth);
             green = color.getGreen();
             //hides second bit in green component
             newGreen = getEmbeddedValue(green,binary.charAt(1),depth);
             blue = color.getBlue();
             //hides third bit in blue component
             newBlue = getEmbeddedValue(blue,binary.charAt(2),depth);
             //combines colour components into a pixel value
             int pixel = (newRed<<16) | (newGreen<<8) | newBlue;
             return pixel;
    }
    //gets the next location in the spiral structure
    private imagePos getNextPos(imagePos imagePos){
        //Depending on direction spiral is travelling in the X or Y position is increased/ decreased
        switch (imagePos.getDirection()){
            case 'S':
                imagePos.incY();
                break;
            case 'E':
                imagePos.incX();
                break;
            case 'N':
                imagePos.decY();
                break;
            case 'W':
                imagePos.decX();
                break;
        }
        //When the distance travelled in a current direction reaches a certain value the direction is changed
        //the direction spiral is travelling in changes and distance travelled reset to 0.
        if(imagePos.getCurrDistance()==imagePos.getMaxLength()){
            imagePos.setCurrDistance(0);
            switch (imagePos.getDirection()){
                case 'S':
                    imagePos.setDirection('E');
                    imagePos.incMaxLength();
                    break;
                case 'E':
                    imagePos.setDirection('N');
                    break;
                case 'N':
                    imagePos.setDirection('W');
                    imagePos.incMaxLength();
                    break;
                case 'W':
                    imagePos.setDirection('S');
                    break;
            }
        }
    return imagePos;
    }
    //hides the contents of a message within an image and returns number of pixels data unable to be hidden in
    public int hideMessage(String msg) {

        int outOfRange=0;
        int pixel,newPixel;
        String asBinary;
        Color color;
        int[] topLeft;
        int[] currPos;
        //the location of the image centre depends on the image's dimensions
        if(width%2==0 && height%2==0){
            topLeft = new int[]{(width/2)-1,(height/2)-2};
        }else if(width%2==0 && height%2!=0){
            topLeft = new int[]{(width/2)-2,(height/2)-1};
        }else{
            topLeft = new int[]{(width/2)-1,(height/2)-1};
        }
        currPos = new int[]{topLeft[0]-1,topLeft[1]};
        imagePos imagePos = new imagePos(currPos,'S',0,3);

        //iterate over the first 10 pixels of spiral since these are used to hide message's metadata
        for(int x = 0;x<10;x++){
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }
        //initiate an arraylist of Strings
        ArrayList<String> binArray = new ArrayList<String>();
        //iterate enough times so that all characters will be hidden
        for(int x = 0;x<Math.ceil((double) msg.length()/(double) bitDepth);x++){
                binArray = new ArrayList<String>();
                //consider the next n characters where n is the number of LSBs being used
                for(int i =0;i<bitDepth;i++) {
                    //ensures not accessing more characters than are available, in case length of message is not exactly divisible by number of LSBs used
                    if(bitDepth*x+i<msg.length()){
                        //converts current characters to binary
                        asBinary= Integer.toString(msg.charAt(bitDepth*x+i),2);
                        StringBuilder sb = new StringBuilder(asBinary);
                        //pad binary with 0s so length is 9
                        for(int z=0;z<9-asBinary.length();z++){
                            sb.insert(0,'0');
                        }
                        asBinary=sb.toString();
                        //add binary value to arraylist
                        binArray.add(asBinary);
                    }
              }
                //iterate over the next 3 pixels (as 3 pixels needed to represent a character)
                for(int y =0;y<3;y++){
                    //iterate over each binary string in arraylist (as 1 pixel can hold information of multiple characters if more than 1 LSB used)
                    for(int i=0;i<binArray.size();i++){
                        currPos = imagePos.getCurrPos();
                        try{
                            //get the current colour of pixel at position
                            pixel = img.getRGB(currPos[0], currPos[1]);
                            color = new Color(pixel, true);
                            //obtain the new pixel colour after embedding a substring of binary at specific bit depth
                            newPixel = getNewColour(color,binArray.get(i).substring(y*3,y*3+3),i+1);
                            img.setRGB(currPos[0], currPos[1], newPixel);
                             }catch(ArrayIndexOutOfBoundsException e){
                                  //if pixel out of image dimensions is accessed then error count is incremented
                                  outOfRange++;
                            }
                    }
                    //loop over the next x pixels in spiral to find next location to hide data in (x is separation between pixels)
                    for(int i=0;i<=separation;i++){
                        imagePos.incCurrDist();
                        getNextPos(imagePos);
                    }
                }
        }
        //returns the number of pixels that weren't able to hide data within
    return outOfRange;
    }
    //checks if there are 2 sequences of bits that represent the centre of an image
    public boolean isDoubleCentre(){
        imageCentre centre1 =findCentre();
        //if 1 centre is found
        if(centre1!=null){
            int centre1Pixel = img.getRGB(centre1.getX(), centre1.getY());
            //replace a specific pixel and search again for another centre
            img.setRGB( centre1.getX(),centre1.getY(),999);
            imageCentre centre2 =findCentre();
            if(centre2 !=null){
                 return true;
            }
            //replace pixel back again
            img.setRGB( centre1.getX(),centre1.getY(),centre1Pixel);

        }
        return false;
    }
    //extracts the content of the secret message in an image
    public String extractData(imageCentre centre) {
        //declare the location of the top left of image's centre
        int[] topLeft = new int[2];
        topLeft[0]=centre.getX();
        topLeft[1]=centre.getY();
        StringBuilder binStr  = new StringBuilder();
        StringBuilder msgStr  = new StringBuilder();
        int pixel,red,green,blue,asciiVal;
        String asBinary;
        StringBuilder sb;
        StringBuilder charBinary = new StringBuilder();
        Color color;
        imageCentre imageCentre = findCentre();
        int[] currPos = new int[]{imageCentre.getX()-1,imageCentre.getY()};
        imagePos imagePos = new imagePos(currPos,'S',0,3);
        //iterate over the first 10 pixels of the spiral as these are used to hide metadata of the message
        for(int x = 0;x<10;x++){
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }
        //iterates required number of times depending on message length of number of LSBs used
        for(int x = 0;x<Math.ceil((double) length/(double) bitDepth);x++){
                binStr.setLength(0);
                //loop over 3 pixels to gain information about characters
                for(int y =0;y<3;y++){
                        currPos = imagePos.getCurrPos();
                        //checks if current location is within image dimensions
                        if(currPos[0]>=0 && currPos[0]<width && currPos[1]>=0 && currPos[1]<height) {
                            //get the pixel value at current location
                            pixel = img.getRGB(currPos[0], currPos[1]);
                            color = new Color(pixel, true);
                            red = color.getRed();
                            //converts red component to binary
                            asBinary = Integer.toString(red, 2);
                            sb = new StringBuilder(asBinary);
                            //pad with 0s
                            for (int z = 0; z < 8 - asBinary.length(); z++) {
                                sb.insert(0, '0');
                            }
                            asBinary = sb.toString();
                            //add the last x bits of the red component to string builder where x is the number of LSBs used
                            for (int i = 1; i <= bitDepth; i++) {
                                binStr.append(asBinary.charAt(asBinary.length() - i));
                            }
                            //same operations with green component
                            green = color.getGreen();
                            asBinary = Integer.toString(green, 2);
                            sb = new StringBuilder(asBinary);
                            for (int z = 0; z < 8 - asBinary.length(); z++) {
                                sb.insert(0, '0');
                            }
                            asBinary = sb.toString();
                            for (int i = 1; i <= bitDepth; i++) {
                                binStr.append(asBinary.charAt(asBinary.length() - i));
                            }
                            //same operations with blue component
                            blue = color.getBlue();
                            asBinary = Integer.toString(blue, 2);
                            sb = new StringBuilder(asBinary);
                            for (int z = 0; z < 8 - asBinary.length(); z++) {
                                sb.insert(0, '0');
                            }
                            asBinary = sb.toString();
                            for (int i = 1; i <= bitDepth; i++) {
                                binStr.append(asBinary.charAt(asBinary.length() - i));

                            }
                        }else{
                               //If location is out of bound of image then add ASCII values that will print underscore character
                            if(y==0){
                                for (int i = 1; i <= bitDepth; i++) {
                                     binStr.append("00");
                                }
                                for (int i = 1; i <= bitDepth; i++) {
                                     binStr.append("1");
                                }
                            }else if(y==1){
                                for (int i = 1; i <= bitDepth; i++) {
                                     binStr.append("0");
                                }
                                for (int i = 1; i <= bitDepth; i++) {
                                     binStr.append("11");
                                }
                            }else{
                                for (int i = 1; i <= bitDepth; i++) {
                                    binStr.append("111");
                                }
                            }
                        }
                    //loop over the next x pixels in spiral to find next location to hide data in (x is separation between pixels)
                    for(int i=0;i<=separation;i++){
                        imagePos.incCurrDist();
                        getNextPos(imagePos);
                    }
                }
                 for(int j=0;j<bitDepth;j++){
                    charBinary.setLength(0);
                    //extract every nth bit from string of bits where n is number of LSBs being used
                    for(int i =0;i<9;i++){
                        charBinary.append(binStr.charAt(i*bitDepth+j));
                    }
                    if(bitDepth*x+j<length){
                        //convert binary value to integer and then to character according to ASCII
                        asciiVal = Integer.parseInt(charBinary.toString(), 2);
                        //add the retrieved character to string builder
                        msgStr.append(Character.toString((char)asciiVal));
                    }
                 }
            }
        //return extracted message
        return msgStr.toString();
    }
}
