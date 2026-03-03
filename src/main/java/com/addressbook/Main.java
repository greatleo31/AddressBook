package com.addressbook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/addressbook/ui/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1100, 700);
            primaryStage.setTitle("通讯录");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("target/error.log"))) {
                e.printStackTrace(pw);
            } catch (Exception ex) {
            }
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
