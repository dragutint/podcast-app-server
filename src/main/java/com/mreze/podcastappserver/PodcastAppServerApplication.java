package com.mreze.podcastappserver;

import javafx.application.Application;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
@Log4j2
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class PodcastAppServerApplication implements CommandLineRunner{

	public static void main(String[] args) {
		//SpringApplication.run(PodcastAppServerApplication.class,args);
		Application.launch(PodcastApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {


	}
}
