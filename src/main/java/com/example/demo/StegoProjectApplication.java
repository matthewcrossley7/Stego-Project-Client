package com.example.demo;

import net.sf.image4j.util.ConvertUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.Principal;


//mvnw clean package
//java -jar demo-0.0.1-SNAPSHOT.jar
@SpringBootApplication
public class StegoProjectApplication {

	public static void main(String[] args) throws IOException {

		/*String name= "C:\\Users\\mcrossley\\Desktop\\temp\\123.png";
		File file= new File(name);
		BufferedImage img = ImageIO.read(file);
		Color whiteCol = new Color (255, 255, 255);

		int height=img.getHeight();
		int width=img.getWidth();
		int pixel,red,green,blue;
		String asBinary;
		int max=0;
		int whitePix = ((255)<<16) | (255<<8) | 255;
		int blackPix = 0;
		int newGreen,newBlue,newRed;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixel = img.getRGB(x,y);
				Color color = new Color(pixel, true);
				red = color.getRed();
				String redBin = Integer.toString(red,2);
				green = color.getGreen();
				String greenBin = Integer.toString(green,2);
				blue = color.getBlue();
				String blueBin = Integer.toString(blue,2);
				if(redBin.charAt(redBin.length()-1)=='1'){
					newRed=255;
				}else{
					newRed=0;
				}
				if(greenBin.charAt(greenBin.length()-1)=='1'){
					newGreen=255;
				}else{
					newGreen=0;
				}
				if(blueBin.charAt(blueBin.length()-1)=='1'){
					newBlue=255;
				}else{
					newBlue=0;
				}
				if(redBin.charAt(redBin.length()-1)=='1' && greenBin.charAt(greenBin.length()-1)=='1' && blueBin.charAt(blueBin.length()-1)=='1'){
				//	img.setRGB(x, y, blackPix);
				}else{
					//img.setRGB(x, y, whitePix);

				}
				//pixel=newBlue;
				pixel =((newRed)<<16) | (newGreen<<8) | newBlue;
				img.setRGB(x, y, pixel);

			}
		}
		System.out.println(max);
		File outPutImage = new File("C:\\Users\\mcrossley\\Desktop\\temp\\RGB.png");
		ImageIO.write(img, "png", outPutImage);
			/*File newfile= new File("C:\\Users\\mcrossley\\Desktop\\temp\\smallcat.png");
		//https://www.tutorialspoint.com/how-to-get-pixels-rgb-values-of-an-image-using-java-opencv-library#:~:text=Retrieving%20the%20pixel%20contents%20(ARGB%20values)%20of%20an%20image%20%E2%88%92&text=Get%20the%20pixel%20value%20at,and%20getBlue()%20methods%20respectively.
		BufferedImage img2 = ImageIO.read(newfile);
		img2 = ConvertUtil.convert24(img2);
		File outPutImage2 = new File("C:\\Users\\mcrossley\\Desktop\\temp\\smallcat24bit.png");
		ImageIO.write(img2, "png", outPutImage2);*/

		SpringApplication.run(StegoProjectApplication.class, args);
	}

}
