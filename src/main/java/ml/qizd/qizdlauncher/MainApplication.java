package ml.qizd.qizdlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
        stage.setTitle("QiZD's Launcher");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("images/icon.png")));
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        Settings.write();
        UserProfiles.write();

        System.exit(0); // TODO: too radical of a solution
    }

    public static void main(String[] args) {
        launch();
    }
}