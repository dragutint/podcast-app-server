package com.mreze.podcastappserver;

import javafx.application.Application;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class PodcastAppServerApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(PodcastAppServerApplication.class,args);
//		Application.launch(PodcastApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		DatagramSocket socket = new DatagramSocket(8081);

		byte[] buffer = new byte[256];

		DatagramPacket request = new DatagramPacket(buffer, buffer.length);
		socket.receive(request);

		InetAddress clientAddress = request.getAddress();
		int clientPort = request.getPort();

		String data = "Message from server";
		buffer = data.getBytes();

		DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
		socket.send(response);
	}
}
