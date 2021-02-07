package com.mreze.podcastappserver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class Controller {

    @FXML
    private Button btnStart;
    private Button btnStop;

    private final AudioRecorder recorder;
    private final Streaming  streaming;
    public Controller() {
        this.recorder = new AudioRecorder();
        this.streaming = new Streaming();
    }

    Thread t1;


    @FXML
    void doSomething(ActionEvent event) {

        t1 = new Thread(new Runnable() {
            public void run()
            {
                streaming.startStreaming();
            }});
        t1.start();
    }

    @FXML
    void  doStop(ActionEvent event)
    {
        streaming.stopStreaming();
    }

}
