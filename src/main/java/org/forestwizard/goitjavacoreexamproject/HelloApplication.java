package org.forestwizard.goitjavacoreexamproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public static final int DEFAULT_WINDOW_WIDTH = 800;
    public static final int DEFAULT_WINDOW_HEIGHT = 600;
    public static final int MIN_WINDOW_WIDTH = 800;
    public static final int MIN_WINDOW_HEIGHT = 600;
    private static final String DEFAULT_STAGE_TITLE = "Wikipedia Desktop";

    @Override
    public void start(Stage stage) throws IOException {
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("search-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        stage.setTitle(DEFAULT_STAGE_TITLE);
        stage.setScene(scene);
        stage.show();
    }
}
