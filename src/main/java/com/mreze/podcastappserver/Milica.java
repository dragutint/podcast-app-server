package com.mreze.podcastappserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import javax.sound.sampled.*;
import java.io.*;


@Component
public class Milica {
    static final long record_time= 10000;

    File wavFile = new File("F:/Backup/FON/Master/Mreže/Projekat/test.wav");

    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    private TargetDataLine line;

    AudioFormat getAudioFormat()
    {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    void start() {
        if (line ==null) {
            try {
                AudioFormat format = getAudioFormat();
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);


                if (!AudioSystem.isLineSupported(info)) {
                    System.out.println("Line not supported");
                    System.exit(0);
                }
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open();
                line.start();

                System.out.println("Start capturing");

                Thread thread = new Thread() {
                    @Override
                    public void run() {

                        AudioInputStream ais = new AudioInputStream(line);

                        System.out.println("Start recording");

                        try {
                            AudioSystem.write(ais, fileType, wavFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
            }
        }
    }

    void finish(){
        if (line!=null) {
            line.stop();
            line.close();
            System.out.println("Finished");
            line = null;
        }
    }

}
