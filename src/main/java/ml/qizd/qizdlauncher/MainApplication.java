package ml.qizd.qizdlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ml.qizd.qizdlauncher.users.UserProfiles;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            Settings.read();
            UserProfiles.read();
        } catch (IOException e) {
            System.out.println("Couldn't load settings and/or user profiles");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        stage.setTitle("QiZD Launcher");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        Settings.write();
        UserProfiles.write();
    }

    public static void main(String[] args) {
        launch();
    }
}