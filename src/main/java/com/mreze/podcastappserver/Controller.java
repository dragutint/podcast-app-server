package com.mreze.podcastappserver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class Controller {

    @FXML
    private Button btnStart;
    private Button btnStop;

    Milica recorder = new Milica();


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
