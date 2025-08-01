package com.example.springapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public DemoApplication(){
	    System.out.println("Hello, World!");
	}

	public static void main(String[] args) {
	    SpringApplication.run(DemoApplication.class, args);
	}

}
