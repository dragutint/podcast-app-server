package com.mreze.podcastappserver;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PodcastAppServerApplication {

	public static void main(String[] args) {
		Application.launch(PodcastApplication.class, args);
	}

}
