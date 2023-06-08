package ml.qizd.qizdlauncher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import ml.qizd.qizdlauncher.apis.AuthLibInjectorDownloader;
import ml.qizd.qizdlauncher.apis.ElyByApi;
import ml.qizd.qizdlauncher.apis.MinecraftDownloader;
import ml.qizd.qizdlauncher.users.ElyByUserProfile;
import ml.qizd.qizdlauncher.users.NoAuthUserProfile;
import ml.qizd.qizdlauncher.users.UserProfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.Set;

public class MainController implements Initializable {
    @FXML
    private TextField login;

    @FXML
    private TextField password;

    @FXML
    private Label welcomeText;

    @FXML
    private Label home_path;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ChoiceBox<UserProfile> profiles;

    @FXML
    private TextField no_auth_name;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        home_path.setText(Settings.getHomePath());
        Settings.read();
        for (UserProfile profile : Settings.getUserProfiles()) {
            if (profile instanceof ElyByUserProfile) {
                try {
                    ElyByApi.refresh((ElyByUserProfile) profile);
                } catch (Exception e) {
                    welcomeText.setText("Failed to refresh ely.by user " + profile);
                    System.out.println(e.getMessage());
                    Settings.removeUser(profile);
                }
            }
        }
        updateProfiles();
    }

    private void updateProfiles() {
        ObservableList<UserProfile> ol = FXCollections.observableArrayList(Settings.getUserProfiles());
        profiles.setItems(ol);
    }

    @FXML
    protected void onAddNoAuthProfileClick() {
        String name = no_auth_name.getText();
        if (name.isBlank() || name.isEmpty()) {
            welcomeText.setText("Username can't be empty");
        }

        welcomeText.setText("Created no auth profile");
        Settings.addUserProfile(new NoAuthUserProfile(name));
        no_auth_name.setText("");
        updateProfiles();
    }

    @FXML
    protected void onSignInButtonCLick() {
        try {
            Settings.addUserProfile(ElyByApi.auth(login.getText(), password.getText()));
            welcomeText.setText("Successfully authenticated");
            login.setText("");
            password.setText("");
        } catch (Exception e) {
            welcomeText.setText("ERROR: " + e.getMessage());
        }

        updateProfiles();
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Downloading Minecraft");

        try {
            MinecraftDownloader.download();
        } catch (Exception e) {
            welcomeText.setText("ERROR: " + e.getMessage());
        }

        welcomeText.setText("Successfully downloaded minecraft");
    }

    @FXML
    protected void onClearButtonClick() {
        Settings.clearUserProfiles();
        updateProfiles();
    }

    @FXML
    protected void onDownloadAuthLibInjectorClick() {
        welcomeText.setText("Downloading authlib injector");

        try {
            AuthLibInjectorDownloader.download();
        } catch (Exception e) {
            welcomeText.setText("ERROR: " + e.getMessage());
        }

        welcomeText.setText("Successfully downloaded authlib injector");
    }

    @FXML
    protected void onSelectHomePathButtonClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(welcomeText.getScene().getWindow());
        if (file == null)
            return;

        Settings.setHomePath(file.getAbsolutePath());
        home_path.setText(Settings.getHomePath());
    }

    @FXML
    protected void onLaunchButtonClick() {
        Path path;
        path = Path.of(Settings.getHomePath(), "launch.bat");

        if (!Files.exists(path)) {
            welcomeText.setText("Download minecraft first");
            return;
        }

        if (profiles.getValue() == null) {
            welcomeText.setText("Create or select profile first");
            return;
        }

        try {
            String command = Files.readString(path);
            command += " " + profiles.getValue().getAuthArgs();
            System.out.println(command);
            Process proc = Runtime.getRuntime().exec(command, new String[]{}, new File(Settings.getHomePath()));
            System.out.println(new String(proc.getInputStream().readAllBytes()));
        } catch (IOException e) {
            welcomeText.setText("ERROR: " + e.getMessage());
        }
    }
}