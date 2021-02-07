package com.mreze.podcastappserver;

import com.mreze.podcastappserver.AudioRecorder;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Streaming{

    private TargetDataLine line;
    Thread t1;

    public void startStreaming()
    {
        try {
        int port = 5555;

        byte[] buffer = new byte[256];
        DatagramSocket socket = new DatagramSocket(port);

                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(request);
                        AudioFormat format = AudioRecorder.getAudioFormat();
                        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                        try {
                            line = (TargetDataLine) AudioSystem.getLine(info);
                            line.open(format);
                        } catch (LineUnavailableException e) {
                            e.printStackTrace();
                        }


                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        int numBytesRead;
                        int CHUNK_SIZE = 1024;
                        byte[] data = new byte[line.getBufferSize() / 5];
                        line.start();

                        for(;;) {
                            numBytesRead = line.read(data, 0, CHUNK_SIZE);
                            out.write(data, 0, numBytesRead);
                            InetAddress address = request.getAddress();
                            port = request.getPort();

                            DatagramPacket response = new DatagramPacket(data,numBytesRead, address, port);
                            socket.send(response);
                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                    }

        } catch (IOException e) {
        e.printStackTrace();
        }

    }

    public void stopStreaming()
    {
        line.stop();
        line.close();
    }

}
