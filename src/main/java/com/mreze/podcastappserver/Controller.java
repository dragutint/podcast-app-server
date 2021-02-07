package com.mreze.podcastappserver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class Controller {

    @FXML
    private Button btnStart;
    private Button btnStop;

    private final AudioRecorder recorder;

    public Controller() {
        this.recorder = new AudioRecorder();
    }


    @FXML
    void doSomething(ActionEvent event) {
        recorder.start();

    }

    @FXML
    void  doStop(ActionEvent event)
    {
        recorder.finish();
    }

}
