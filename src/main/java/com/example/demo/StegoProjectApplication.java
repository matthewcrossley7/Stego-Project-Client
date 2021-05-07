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
//initiates the application
@SpringBootApplication
public class StegoProjectApplication {

	public static void main(String[] args) throws IOException {

		SpringApplication.run(StegoProjectApplication.class, args);
	}

}
