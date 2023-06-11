package ml.qizd.qizdlauncher.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import ml.qizd.qizdlauncher.Settings;
import ml.qizd.qizdlauncher.apis.ElyByApi;
import ml.qizd.qizdlauncher.users.ElyByUserProfile;
import ml.qizd.qizdlauncher.users.NoAuthUserProfile;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

public class SettingsController implements Initializable {
    private final MainController parent;

    @FXML
    private Label home_path_label;
    @FXML
    private Button select_home_path_button;

    @FXML
    private TextField no_auth_nickname_textfield;
    @FXML
    private Button no_auth_add_button;

    @FXML
    private TextField ely_by_login_textfield;
    @FXML
    private TextField ely_by_password_textfield;
    @FXML
    private Button ely_by_sign_in_button;

    private void showError(String e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e, ButtonType.OK);
        alert.show();
    }

    private void showInformation(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, s, ButtonType.OK);
        alert.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        home_path_label.setText(Settings.getHomePath());
        select_home_path_button.setOnAction((e) -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(Settings.getHomePath()));
            File dir = chooser.showDialog(this.home_path_label.getScene().getWindow());
            Settings.setHomePath(dir.getPath());

            home_path_label.setText(Settings.getHomePath());
        });

        no_auth_add_button.setOnAction((e) -> {
            if (no_auth_nickname_textfield.getText().isEmpty())
                return;

            Settings.addUserProfile(new NoAuthUserProfile(no_auth_nickname_textfield.getText()));
            showInformation("Пользователь успешно добавлен");
            no_auth_nickname_textfield.setText("");
            parent.updateUsers();
        });

        ely_by_sign_in_button.setOnAction((e) -> {
            if (ely_by_login_textfield.getText().isEmpty() || ely_by_password_textfield.getText().isEmpty())
                return;

            try {
                Settings.addUserProfile(ElyByApi.auth(ely_by_login_textfield.getText(), ely_by_password_textfield.getText()));
                showInformation("Пользователь успешно добавлен");
                ely_by_login_textfield.setText("");
                ely_by_password_textfield.setText("");
                parent.updateUsers();
            } catch (Exception ex) {
                showError(ex.toString());
                ely_by_password_textfield.setText("");
            }
        });
    }

    public SettingsController(MainController parent) {
        this.parent = parent;
    }
}
