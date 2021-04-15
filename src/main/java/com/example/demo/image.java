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
   // private FileWriter writer;
    private int height;
    private int width;
    private int middleBlockSize=3;
    private int bitDepth;
    private int seperation;
    public image(String name) {
        this.name=name;
    }
    public image() {

    }
    public void rotateCw( )
    {
        int newWidth  = img.getWidth();
        int newHeight = img.getHeight();
        BufferedImage   newImage = new BufferedImage( newHeight, newWidth, img.getType() );
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
    public void setString(String imageString) throws IOException {
        byte[] imageByteArray = decodeImage(imageString);

        InputStream is = new ByteArrayInputStream(imageByteArray);
        img = ImageIO.read(is);

        height=img.getHeight();
        width=img.getWidth();
    }
    public void readImage() throws IOException {
      //  writer = new FileWriter("C:\\Users\\mcrossley\\eclipse-workspace\\StegoProj\\src\\pixel_values.txt");
        //Reading the image
        File file= new File(name);
        //https://www.tutorialspoint.com/how-to-get-pixels-rgb-values-of-an-image-using-java-opencv-library#:~:text=Retrieving%20the%20pixel%20contents%20(ARGB%20values)%20of%20an%20image%20%E2%88%92&text=Get%20the%20pixel%20value%20at,and%20getBlue()%20methods%20respectively.
        img = ImageIO.read(file);
        img = ConvertUtil.convert24(img);
        //saveNewImage("C:\\Users\\mcrossley\\Desktop\\temp\\new24bit.png");
        height=img.getHeight();
        width=img.getWidth();


    }


    public void readPixels() throws IOException {
        int max=0;
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
                // alpha=color.getAlpha();

                if(red>max) {
                    max=red;
                }
             //  writer.append(red+":");
              //  writer.append(green+":");
              //  writer.append(blue+"");
                // writer.append(alpha+"");
               // writer.append("\n");
               // writer.flush();
                pixel = 0;
                //red+=150;
                if(red>255) {
                    red=255;
                }
                pixel =  ((red)<<16) | (green<<8) | blue;
                img.setRGB(x, y, pixel);

            }
        }

       // writer.close();
        System.out.println("RGB values at each pixel are stored in the specified file" + max+"MAX");
    }
    private int getMaxSeperation(int msgLength,int bitDepth){
       // System.out.println(Math.pow(Math.min(height, width),2)*bitDepth + "  AND   " + msgLength*3);
       // System.out.println("ONE IS  " +  Math.floor(((double)height*width*bitDepth-20)/(double)(msgLength*3))+ "  OTHER IS " + Math.floor(((double)height*width*bitDepth-20)/(double)(msgLength*3)));
        int temp = bitDepth*((int) Math.floor(((double)Math.pow(Math.min(height, width),2)-19)/(double)(msgLength*3))-1);
        if(temp>=4096){
            temp=4095;
        }
        System.out.println("MAKING SEPERATION " + temp);
        return temp;
    }
    public void hidemsgLength(int length,int bitDepth,int seperation) {
        String binary = Integer.toString(length,2);
        if(seperation==-1){
            seperation = getMaxSeperation(length,bitDepth);
        }
         int pixel;
        String asBinary;
       // System.out.println("TOP LEFT IS "+topLeft[0]+"   "+topLeft[1]);
        int[] topLeft = new int[]{(width/2)-1,(height/2)-1};
        int[] currPos = new int[]{topLeft[0]-1,topLeft[1]};
        imagePos imagePos = new imagePos(currPos,'S',0,3);
        Color color;
        StringBuilder sb = new StringBuilder(binary);
        for(int z=0;z<15-binary.length();z++){
            sb.insert(0,'0');
        }
        System.out.println(sb);
        binary=sb.toString();
        for(int x = 0;x<5;x++){
            asBinary=binary.substring(x*3,x*3+3);

            currPos = imagePos.getCurrPos();
          //  System.out.println("HIDING At "+ currPos[0] + "    "+currPos[1]+" going "+imagePos.getDirection());

            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            int newPixel = getNewColour(color,asBinary,1);
            img.setRGB(currPos[0], currPos[1], newPixel);
           // color = new Color(newPixel, true);
            //  System.out.println(asBinary+"    "+ asBinary.substring(y*3,y*3+3)+"   " +Integer.toString(color.getRed(),2) + "   " + Integer.toString(color.getGreen(),2)+ "    "+Integer.toString(color.getBlue(),2)+  "  "+color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
            //pixel = img.getRGB(currPos[0], currPos[1]);
           // color = new Color(pixel, true);
          //  System.out.println(color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
            //currDistance++;
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }
        currPos = imagePos.getCurrPos();
        //  System.out.println("HIDING At "+ currPos[0] + "    "+currPos[1]+" going "+imagePos.getDirection());
        this.bitDepth=bitDepth;
        pixel = img.getRGB(currPos[0], currPos[1]);
        color = new Color(pixel, true);
        binary = Integer.toString(bitDepth-1,2);
        sb = new StringBuilder(binary);
        for(int z=0;z<3-binary.length();z++){
            sb.insert(0,'0');
        }

        int newPixel = getNewColour(color,sb.toString(),1);
        img.setRGB(currPos[0], currPos[1], newPixel);
        System.out.println("BIT BIN"+ sb);

        imagePos.incCurrDist();
        getNextPos(imagePos);
        currPos = imagePos.getCurrPos();
        this.seperation=seperation;

        binary = Integer.toString(seperation,2);
        sb = new StringBuilder(binary);
        for(int z=0;z<12-binary.length();z++){
            sb.insert(0,'0');
        }
        System.out.println("SEP BIN IS "+sb+ " part 1 "+ sb.toString().substring(0,3)+"   "+sb.toString().substring(3,6) );
      /*  pixel = img.getRGB(currPos[0], currPos[1]);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,sb.toString().substring(0,3),1);
        img.setRGB(currPos[0], currPos[1], newPixel);

        imagePos.incCurrDist();
        getNextPos(imagePos);
        currPos = imagePos.getCurrPos();
        pixel = img.getRGB(currPos[0], currPos[1]);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,sb.toString().substring(3,6),1);
        img.setRGB(currPos[0], currPos[1], newPixel);

        imagePos.incCurrDist();
        getNextPos(imagePos);
        currPos = imagePos.getCurrPos();
        pixel = img.getRGB(currPos[0], currPos[1]);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,sb.toString().substring(6,9),1);
        img.setRGB(currPos[0], currPos[1], newPixel);

        imagePos.incCurrDist();
        getNextPos(imagePos);
        currPos = imagePos.getCurrPos();
        pixel = img.getRGB(currPos[0], currPos[1]);
        color = new Color(pixel, true);
        newPixel = getNewColour(color,sb.toString().substring(9,12),1);
        img.setRGB(currPos[0], currPos[1], newPixel);
*/

        for(int x = 0;x<4;x++){
            asBinary=sb.toString().substring(x*3,x*3+3);

            currPos = imagePos.getCurrPos();
            //  System.out.println("HIDING At "+ currPos[0] + "    "+currPos[1]+" going "+imagePos.getDirection());

            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            newPixel = getNewColour(color,asBinary,1);
            img.setRGB(currPos[0], currPos[1], newPixel);
            // color = new Color(newPixel, true);
            //  System.out.println(asBinary+"    "+ asBinary.substring(y*3,y*3+3)+"   " +Integer.toString(color.getRed(),2) + "   " + Integer.toString(color.getGreen(),2)+ "    "+Integer.toString(color.getBlue(),2)+  "  "+color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
            //pixel = img.getRGB(currPos[0], currPos[1]);
            // color = new Color(pixel, true);
            //  System.out.println(color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
            //currDistance++;
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }

        // int newPixel = getNewColour(color,asBinary);
       // img.setRGB(currPos[0], currPos[1], newPixel);
    //    System.out.println("ENCODED WITH LENGTH  "+Integer.parseInt(binary.toString(), 2));

    }
    public int getMsgLength(imageCentre centre) {
        int pixel,red,green,blue;
        StringBuilder str  = new StringBuilder();
        StringBuilder str2  = new StringBuilder();
        StringBuilder str3  = new StringBuilder();
        String asBinary;
        char direction='S';
        int maxLength=3;
        int currDistance=0;
        Color color;
        int[] topLeft = new int[]{(width/2)-1,(height/2)-1};
        topLeft[0]=centre.getX();
        topLeft[1]=centre.getY();
        int[] currPos = new int[]{topLeft[0]-1,topLeft[1]};

        imagePos imagePos = new imagePos(currPos,'S',0,3);

        for(int x = 0;x<5;x++){
            currPos = imagePos.getCurrPos();
            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            red = color.getRed();
            asBinary = Integer.toString(red,2);
            str.append(asBinary.charAt(asBinary.length()-1));
            green = color.getGreen();
            asBinary = Integer.toString(green,2);
            str.append(asBinary.charAt(asBinary.length()-1));
            blue = color.getBlue();
            asBinary = Integer.toString(blue,2);
            str.append(asBinary.charAt(asBinary.length()-1));
            imagePos.incCurrDist();
            getNextPos(imagePos);
         //   System.out.println("NOTHIDING At "+ currPos[0] + "    "+currPos[1]+" going "+direction);
           // System.out.println(red+"   "+green+"   "+blue+" ++" + str);

        }
        currPos = imagePos.getCurrPos();
        pixel = img.getRGB(currPos[0], currPos[1]);
        color = new Color(pixel, true);
        red = color.getRed();
        asBinary = Integer.toString(red,2);
        str2.append(asBinary.charAt(asBinary.length()-1));
        green = color.getGreen();
        asBinary = Integer.toString(green,2);
        str2.append(asBinary.charAt(asBinary.length()-1));
        blue = color.getBlue();
        asBinary = Integer.toString(blue,2);
        str2.append(asBinary.charAt(asBinary.length()-1));
        bitDepth= Integer.parseInt(str2.toString(),2)+1;
        System.out.println("LENGTH IS " +bitDepth);
        length = Integer.parseInt(str.toString(), 2);
        // System.out.println("STRING IS " + str + " LENGTH " + length);
        System.out.println("DECODED LENGTH IS "+length);

        for(int i=0;i<4;i++){
            imagePos.incCurrDist();
            getNextPos(imagePos);
            currPos = imagePos.getCurrPos();
            pixel = img.getRGB(currPos[0], currPos[1]);
            color = new Color(pixel, true);
            str3.append(getBits(color));
        }
        seperation= Integer.parseInt(str3.toString(),2);
        System.out.println("SEPEATUON IS "+seperation);
        return length;

    }
    public String getBits(Color color){
        StringBuilder str = new StringBuilder();
        String asBinary;
        int red,green,blue;
        red = color.getRed();
        asBinary = Integer.toString(red,2);
        str.append(asBinary.charAt(asBinary.length()-1));
        green = color.getGreen();
        asBinary = Integer.toString(green,2);
        str.append(asBinary.charAt(asBinary.length()-1));
        blue = color.getBlue();
        asBinary = Integer.toString(blue,2);
        str.append(asBinary.charAt(asBinary.length()-1));
        return str.toString();
    }


    public void saveNewImage(String fileName) throws IOException {
        File outPutImage = new File(fileName);
        ImageIO.write(img, "png", outPutImage);
        System.out.println("NEW IMAGE SAVED AT DESTINATION " +fileName);
    }
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
        return null;
    }
    public imageCentre findCentre(){
        int pixel,red,green,blue;
        int countx,county;
        boolean valid;
        boolean isCentre=false;
        String redAsBinary,greenAsBinary,blueAsBinary;

        for(int x=0;x<width-middleBlockSize;x++) {
            for(int y=0;y<height-middleBlockSize;y++) {
                countx=0;
                county=0;
                valid = true;
                do{
                    pixel = img.getRGB(x+countx, y+county);
                    //pixel = 0;
                    Color color = new Color(pixel, true);
                    red = color.getRed();
                    redAsBinary = Integer.toString(red,2);
                    green = color.getGreen();
                    greenAsBinary = Integer.toString(green,2);
                    blue = color.getBlue();
                    blueAsBinary = Integer.toString(blue,2);
                    if(countx==0 && county==0) {
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
                   //     System.out.println("FOUND CENTER AT " + x+"   "+y);
                    //    System.out.println(x + "  " +y + "  "+countx +"  " + county +"  "+ redAsBinary+ "  "+greenAsBinary+"   "+blueAsBinary );
                        county++;
                        return new imageCentre(x,y,0);
                    }else if(countx<middleBlockSize-1){
                        countx++;
                    }else{
                        countx=0;
                        county++;
                    }

                }while(valid);

            }

        }
        System.out.println("no center found");
        return null;
    }
    public int[] getXY(int position){
        int[] xy = new int[2];
        xy[0] = (int)position%width;
        xy[1] = (int)Math.floor((double)position/(double)width);

        //System.out.println(position + "  x " + xy[0] + "  y" + xy[1]);
        return xy;
    }
    public int getEmbeddedValue(int colour,char bit,int depth) {

        String asBinary = Integer.toString(colour,2);
        StringBuilder sb = new StringBuilder(asBinary);
        for(int z=0;z<8-asBinary.length();z++){
            sb.insert(0,'0');
        }
        asBinary=sb.toString();
       // System.out.println("   INITIAL" + asBinary + " AT DEPTH  " +depth + " BEW BIT "+bit);
        String newBinary = asBinary.substring(0, asBinary.length()-depth)+bit;
        if(depth>1){
       //     System.out.println("END BIT "+asBinary.substring(asBinary.length()-depth,asBinary.length()-1) );
            newBinary=newBinary+asBinary.substring(asBinary.length()-depth+1,asBinary.length());
        }
       // System.out.println("AFTER     " + newBinary + " AT DEPTH  " +depth);

        return Integer.parseInt(newBinary, 2);
    }
public int getNewColour(Color color, String binary,int depth){
        int blue,green,red,newBlue,newGreen,newRed;
    red = color.getRed();
    //asBinary = Integer.toString(red,2);
    newRed = getEmbeddedValue(red,binary.charAt(0),depth);
    //System.out.println(Integer.toString(red,2) + "   " + Integer.toString(newRed,2) + "   "+binary.charAt(0));
    green = color.getGreen();
    //asBinary = Integer.toString(green,2);
    newGreen = getEmbeddedValue(green,binary.charAt(1),depth);
  //  System.out.println(Integer.toString(green,2) + "   " + Integer.toString(newGreen,2) + "   "+binary.charAt(1));
    blue = color.getBlue();
    //colBinary = Integer.toString(blue,2);
    // System.out.println("RED IS " + red + " BINARY " + asBinary + " EMBEDDING " + binary.charAt(x));
    newBlue = getEmbeddedValue(blue,binary.charAt(2),depth);
   // System.out.println(Integer.toString(blue,2) + "   " + Integer.toString(newBlue,2) + "   "+binary.charAt(2));
    int pixel = (newRed<<16) | (newGreen<<8) | newBlue;

return pixel;

}
    private imagePos getNextPos(imagePos imagePos){

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
        if(imagePos.getCurrDistance()==imagePos.getMaxLength()){
            imagePos.setCurrDistance(0);
         //   System.out.println("CHANGED DIRECTION");
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
    public int hideData(String msg) {

        int outOfRange=0;
        int pixels;
        Color colors;
        int pixel,red,green,blue;
        char character;
        int newRed, newGreen,newBlue;
        int ascii;
        String asBinary;
        String newBinary;
        String colBinary;
        int count;
        char direction;
        direction='S';
        Color color;
        int[] topLeft = new int[]{(width/2)-1,(height/2)-1};
        int[] currPos = new int[]{topLeft[0]-1,topLeft[1]};
        imagePos imagePos = new imagePos(currPos,'S',0,3);

        int newPixel;


        for(int x=0;x<3;x++){
            for(int y=0;y<3;y++){
                if(x==0 &&y ==0){
                    pixel = img.getRGB(topLeft[0], topLeft[1]);
                    color = new Color(pixel, true);
                    newPixel = getNewColour(color,"010",1);
                    img.setRGB( topLeft[0],topLeft[1],newPixel);

                }else{
                    pixel = img.getRGB(topLeft[0]+x, topLeft[1]+y);
                    color = new Color(pixel, true);
                    newPixel = getNewColour(color,"101",1);
                    img.setRGB(topLeft[0]+x,topLeft[1]+y, newPixel);
                   // System.out.println(" at x "+x+" y " +y + " 010");
                }
            }
        }
        /* embedding another centre for testing
        for(int x=0;x<3;x++){
            for(int y=0;y<3;y++){
                if(x==0 &&y ==0){
                    pixel = img.getRGB(10, 10);
                    color = new Color(pixel, true);
                    newPixel = getNewColour(color,"001",1);
                   img.setRGB( 10,10,newPixel);
                }else if((x+y)%2==0){
                    pixel = img.getRGB(10+x, 10+y);
                    color = new Color(pixel, true);
                    newPixel = getNewColour(color,"101",1);
                   img.setRGB( 10+x,10+y, newPixel);

                }else{
                    pixel = img.getRGB(10+x, 10+y);
                    color = new Color(pixel, true);
                    newPixel = getNewColour(color,"010",1);
                    img.setRGB(10+x,10+y, newPixel);

                }
            }
        }*/

        for(int x = 0;x<10;x++){
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }
        ArrayList<String> binArray = new ArrayList<String>();
        int depth = 1;
        System.out.println(msg.length()+"  000 --  "+ bitDepth);
        for(int x = 0;x<Math.ceil((double) msg.length()/(double) bitDepth);x++){
          //  System.out.println(x+"XXXX");
            binArray = new ArrayList<String>();
                for(int i =0;i<bitDepth;i++) {
                    if(bitDepth*x+i<msg.length()){
                        asBinary= Integer.toString(msg.charAt(bitDepth*x+i),2);

                        StringBuilder sb = new StringBuilder(asBinary);
                        for(int z=0;z<9-asBinary.length();z++){
                            sb.insert(0,'0');
                        }
                        System.out.println(asBinary);
                        asBinary=sb.toString();
                      //  System.out.println("ADDED ARRAYLIST  " + msg.charAt(bitDepth*x+i)+ " with bin "+asBinary+" to pos "+i);
                        binArray.add(asBinary);
                    }

              }
                //System.out.println("EMBEDDING "+msg.charAt(x)+ " bit depth"+depth + "   "+ currPos[0]+ "   "+currPos[1]);

                // asBinary="000";
                //  System.out.println("FINAL  "+asBinary);
                for(int y =0;y<3;y++){
                    for(int i=0;i<binArray.size();i++){
                        currPos = imagePos.getCurrPos();
                        try{
                            pixel = img.getRGB(currPos[0], currPos[1]);
                            color = new Color(pixel, true);

                            //newPixel = getNewColour(color,asBinary.substring(y*3,y*3+3),depth);
                          //  System.out.println("EMBEDDING "+binArray.get(i).substring(y*3,y*3+3)+ " bit depth"+(i+1) + "   "+ currPos[0]+ "   "+currPos[1]);

                            newPixel = getNewColour(color,binArray.get(i).substring(y*3,y*3+3),i+1);

                            img.setRGB(currPos[0], currPos[1], newPixel);
                            System.out.println("at "+currPos[0]+ "   "+ currPos[1]+ " binary " +binArray.get(i) );
                            color = new Color(newPixel, true);
                            //System.out.println(asBinary+"    "+ asBinary.substring(y*3,y*3+3)+"   " +Integer.toString(color.getRed(),2) + "   " + Integer.toString(color.getGreen(),2)+ "    "+Integer.toString(color.getBlue(),2)+  "  "+color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
                            pixel = img.getRGB(currPos[0], currPos[1]);
                            color = new Color(pixel, true);
                            // System.out.println(color.getRed() +"  "+color.getGreen() +"  "+color.getBlue());
                            }catch(ArrayIndexOutOfBoundsException e){
                                System.out.println("Cant hide all of message");
                                outOfRange++;
                            }
                    }
                   /* String newBin = Integer.toString(pixel,2);
                    StringBuilder sb = new StringBuilder(newBin);
                    for(int z=0;z<8-sb.length();z++){
                        sb.insert(0,'0');
                    }
                    asBinary=sb.toString();
                    System.out.println("new pixel bin is "+asBinary);*/
                    for(int i=0;i<=seperation;i++){
                        imagePos.incCurrDist();
                        getNextPos(imagePos);
                    }

                }




        }
return outOfRange;
    // System.out.println("Height" + height+ "  width"+width);
       // System.out.println("Height2    " + height/2+ "  width"+width/2);
    }
    public boolean isDoubleCentre(){
        imageCentre centre1 =findCentre();
        if(centre1!=null){
            System.out.println("THERE IS AT LEAST 1 CENTRE at "+centre1.getX() + " " +centre1.getY());
            int centre1Pixel = img.getRGB(centre1.getX(), centre1.getY());
            System.out.println(centre1Pixel+ " IDKK222KK");
            Color color = new Color(centre1Pixel, true);
            int tempCentrePixel = getNewColour(color,"000",1);
            img.setRGB( centre1.getX(),centre1.getY(),999);
            int idkint = img.getRGB(centre1.getX(), centre1.getY());
            System.out.println(idkint+ " IDKKKK");
            imageCentre centre2 =findCentre();
            if(centre2 !=null){
                System.out.println("THERE IS AT LEAST 2 CENTRE"+centre2.getX() + " " +centre2.getY());
                return true;
            }
            img.setRGB( centre1.getX(),centre1.getY(),centre1Pixel);

        }
        return false;
    }
    public String extractData(imageCentre centre) {
        int[] topLeft = new int[]{(width/2)-1,(height/2)-1};
        topLeft[0]=centre.getX();
        topLeft[1]=centre.getY();
        System.out.println("ENCODIGN WITH TOP LEFT AT " + topLeft[0] + "  y "+topLeft[1]);
        int pixels;
        Color colors;
        StringBuilder binStr  = new StringBuilder();
        StringBuilder msgStr  = new StringBuilder();

        int pixel,red,green,blue,asciiVal;
        char character;
        int newRed, newGreen,newBlue;
        int ascii;
        String asBinary;
        String newBinary;
        String colBinary;
        int count;
        StringBuilder sb;
        StringBuilder charBinary = new StringBuilder();
        Color color;
        imageCentre imageCentre = findCentre();
        char direction ='S';
        int[] currPos = new int[]{imageCentre.getX()-1,imageCentre.getY()};
        imagePos imagePos = new imagePos(currPos,'S',0,3);

        System.out.println("STARTING AT "+currPos[0]+"   "+currPos[1]);
        int maxLength=3;
        int currDistance=0;
        System.out.println("EXTRACTING MSG LEGNTH "+length);
        for(int x = 0;x<10;x++){
            imagePos.incCurrDist();
            getNextPos(imagePos);
        }
        ArrayList<String> binArray = new ArrayList<String>();

        for(int x = 0;x<Math.ceil((double) length/(double) bitDepth);x++){

                binStr.setLength(0);

                for(int y =0;y<3;y++){

                        currPos = imagePos.getCurrPos();
                        if(currPos[0]>=0 && currPos[0]<width && currPos[1]>=0 && currPos[1]<height) {


                            pixel = img.getRGB(currPos[0], currPos[1]);
                            System.out.println("now at "+currPos[0]+ "  "+ currPos[1]);
                            color = new Color(pixel, true);
                            red = color.getRed();
                            asBinary = Integer.toString(red, 2);
                            sb = new StringBuilder(asBinary);
                            for (int z = 0; z < 8 - asBinary.length(); z++) {
                                sb.insert(0, '0');
                            }
                            asBinary = sb.toString();

                            for (int i = 1; i <= bitDepth; i++) {
                                binStr.append(asBinary.charAt(asBinary.length() - i));
                                // System.out.println("found " +asBinary.charAt(asBinary.length()-i)+ " at "+i );
                            }

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
                                //Trying to print underscore if out of range
                            for (int i = 1; i <= bitDepth; i++) {
                                binStr.append("00");
                            }
                            for (int i = 1; i <= bitDepth; i++) {
                                binStr.append("1");
                            }
                            for (int i = 1; i <= bitDepth; i++) {
                                binStr.append("0");
                            }
                            for (int i = 1; i <= bitDepth; i++) {
                                binStr.append("11111");
                            }
                            //System.out.println("I GOT"+binStr);
                        }

                    for(int i=0;i<=seperation;i++){
                        imagePos.incCurrDist();
                        getNextPos(imagePos);
                    }
                }

                 for(int j=0;j<bitDepth;j++){
                     charBinary.setLength(0);
                    for(int i =0;i<9;i++){
                        charBinary.append(binStr.charAt(i*bitDepth+j));
                    }
                    if(bitDepth*x+j<length){
                        asciiVal = Integer.parseInt(charBinary.toString(), 2);
                  //      System.out.println("CHAR IS " + Character.toString((char)asciiVal) + " bin "+charBinary);
                        msgStr.append(Character.toString((char)asciiVal));
                        System.out.println(msgStr);
                    }

                 }

        }

            System.out.println("FINAL MSG "+msgStr);
        return msgStr.toString();
    }
}
