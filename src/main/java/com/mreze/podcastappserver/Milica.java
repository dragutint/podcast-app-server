package com.mreze.podcastappserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sound.sampled.*;
import java.io.*;

@Component
public class Milica implements CommandLineRunner {
    static final long record_time= 10000;

    File wavFile = new File("F:/Backup/FON/Master/Mreže/Projekat/test.wav");

    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    TargetDataLine line;

    AudioFormat getAudioFormat()
    {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    void start() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)){
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("Start capturing");

            AudioInputStream ais = new AudioInputStream(line);

            System.out.println("Start recording");

            AudioSystem.write(ais, fileType, wavFile);
        }catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void finish(){
        line.stop();
        line.close();
        System.out.println("Finished");
    }

    @Override
    public void run(String... args) throws Exception {
        final Milica recorder = new Milica();

        Thread stopper = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(record_time);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                recorder.finish();
            }
        });

        stopper.start();

        recorder.start();
    }
}
