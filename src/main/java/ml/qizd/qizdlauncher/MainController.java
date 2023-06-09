package ml.qizd.qizdlauncher;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import ml.qizd.qizdlauncher.apis.AuthLibInjectorDownloader;
import ml.qizd.qizdlauncher.apis.ElyByApi;
import ml.qizd.qizdlauncher.apis.FabricApi;
import ml.qizd.qizdlauncher.apis.MinecraftApi;
import ml.qizd.qizdlauncher.models.FabricMeta;
import ml.qizd.qizdlauncher.models.VersionInfo;
import ml.qizd.qizdlauncher.users.ElyByUserProfile;
import ml.qizd.qizdlauncher.users.NoAuthUserProfile;
import ml.qizd.qizdlauncher.users.UserProfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    Thread downloadThread = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Settings.read();
    }
}