package com.mreze.podcastappserver;

import javax.sound.sampled.*;
import java.io.*;

public class AudioRecorder {
    File wavFile = new File("F:/Backup/FON/Master/Mreže/Projekat/test.wav");

    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    private TargetDataLine line;

    public static AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
    }

    public void start() {
        if (line == null) {
            try {
                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);


                if (!AudioSystem.isLineSupported(info)) {
                    System.out.println("Line not supported");
                    System.exit(0);
                }
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open();
                line.start();

                System.out.println("Start capturing");

                Thread thread = new Thread(() -> {

                    AudioInputStream ais = new AudioInputStream(line);

                    System.out.println("Start recording");


                });
                thread.start();
            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void finish() {
        if (line != null) {
            line.stop();
            line.close();
            System.out.println("Finished");
            line = null;
        }
    }

}
