package com.tstu;

import com.tstu.backend.ILexicalAnalyzer;
import com.tstu.backend.lexems.LexicalAnalyzer;
import com.tstu.controllers.MainWindow;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static Stage primaryStage;
    private static MainWindow mainWindow;

    public static MainWindow getMainWindow() {
        return mainWindow;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("fxml/MainWindow.fxml").openStream());
        mainWindow = (MainWindow) fxmlLoader.getController();
        primaryStage.setTitle("Транслятор");
        primaryStage.setScene(new Scene(root));
        runStage(primaryStage);

    }

    public static void runStage(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
