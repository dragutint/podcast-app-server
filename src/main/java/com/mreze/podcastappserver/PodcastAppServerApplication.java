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
		SpringApplication.run(PodcastAppServerApplication.class,args);
//		Application.launch(PodcastApplication.class, args);
	}

	private TargetDataLine line;

	@Override
	public void run(String... args) throws Exception {

		String hostname = "localhost";
		int port = 5555;
		InetAddress address = InetAddress.getByName(hostname);
		byte[] buffer = new byte[256];
		DatagramSocket socket = new DatagramSocket(port);


		DatagramPacket request = new DatagramPacket(buffer, buffer.length);
		socket.receive(request);

		AudioFormat format = AudioRecorder.getAudioFormat();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		line = (TargetDataLine) AudioSystem.getLine(info);
		line.open(format);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int numBytesRead;
		int CHUNK_SIZE = 1024;
		byte[] data = new byte[line.getBufferSize() / 5];
		line.start();


		for(;;) {
			numBytesRead = line.read(data, 0, CHUNK_SIZE);
			out.write(data, 0, numBytesRead);
			address = request.getAddress();
			port = request.getPort();

			DatagramPacket response = new DatagramPacket(data,numBytesRead, address, port);
			socket.send(response);

		}

	}
}
