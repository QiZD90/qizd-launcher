package ml.qizd.qizdlauncher.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import ml.qizd.qizdlauncher.Settings;
import ml.qizd.qizdlauncher.apis.ElyByApi;
import ml.qizd.qizdlauncher.users.NoAuthUserProfile;
import ml.qizd.qizdlauncher.users.UserProfile;
import ml.qizd.qizdlauncher.users.UserProfiles;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private static class UserProfileListViewEntry extends HBox {
        UserProfile profile;

        private static UserProfileListViewEntry create(UserProfile profile) {
            Label label = new Label(profile.toString());
            label.setAlignment(Pos.CENTER);
            Button button = new Button("❌");
            button.setOnAction((e) -> UserProfiles.removeUser(profile));

            return new UserProfileListViewEntry(profile, label, button);
        }

        private UserProfileListViewEntry(UserProfile profile, Node... nodes) {
            super(8d, nodes);
            this.profile = profile;
        }
    }

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
    @FXML
    private Tab accounts_tab;

    private void showExceptionError(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, Arrays.toString(e.getStackTrace()), ButtonType.OK);
        alert.show();
    }

    private void showInformation(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, s, ButtonType.OK);
        alert.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<UserProfileListViewEntry> entries = FXCollections.observableArrayList(
                UserProfiles.getProfiles().stream().map(UserProfileListViewEntry::create).toList()
        );
        UserProfiles.getProfiles().addListener((ListChangeListener<UserProfile>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    entries.addAll(c.getAddedSubList().stream().map(UserProfileListViewEntry::create).toList());
                }

                if (c.wasRemoved()) {
                    entries.removeIf(e -> c.getRemoved().contains(e.profile));
                }
            }
        });
        accounts_tab.setContent(new ListView<>(entries));

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

            UserProfiles.addUserProfile(new NoAuthUserProfile(no_auth_nickname_textfield.getText()));
            showInformation("Пользователь успешно добавлен");
            no_auth_nickname_textfield.setText("");
            parent.updateUsers();
        });

        ely_by_sign_in_button.setOnAction((e) -> {
            if (ely_by_login_textfield.getText().isEmpty() || ely_by_password_textfield.getText().isEmpty())
                return;

            try {
                UserProfiles.addUserProfile(ElyByApi.auth(ely_by_login_textfield.getText(), ely_by_password_textfield.getText()));
                showInformation("Пользователь успешно добавлен");
                ely_by_login_textfield.setText("");
                ely_by_password_textfield.setText("");
                parent.updateUsers();
            } catch (Exception ex) {
                showExceptionError(ex);
                ely_by_password_textfield.setText("");
            }
        });
    }

    public SettingsController(MainController parent) {
        this.parent = parent;
    }
}
