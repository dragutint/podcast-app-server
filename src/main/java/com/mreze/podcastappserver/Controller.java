package com.mreze.podcastappserver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class Controller {

    @FXML
    private Button btnAction1;

    @FXML
    void doSomething(ActionEvent event) {
        System.out.println("click");
    }

}
