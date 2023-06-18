package ml.qizd.qizdlauncher.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ml.qizd.qizdlauncher.*;
import ml.qizd.qizdlauncher.apis.AuthLibInjectorDownloader;
import ml.qizd.qizdlauncher.apis.FabricApi;
import ml.qizd.qizdlauncher.apis.MinecraftApi;
import ml.qizd.qizdlauncher.apis.ModpackApi;
import ml.qizd.qizdlauncher.models.*;
import ml.qizd.qizdlauncher.users.UserProfile;
import ml.qizd.qizdlauncher.users.UserProfiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public final Image icon = new Image(MainApplication.class.getResourceAsStream("images/icon.png"));
    @FXML
    private Pane root;
    @FXML
    private Label title_label;
    @FXML
    private Label splash_label;
    @FXML
    private ChoiceBox<UserProfile> user_profiles_choicebox;
    @FXML
    private Button launch_button;
    @FXML
    private Button minecraft_button;
    @FXML
    private Button modpack_button;
    @FXML
    private Button settings_button;
    @FXML
    private ProgressBar progress_bar;
    @FXML
    private Label progress_label;

    private List<Button> buttonsToDisable = null;

    private void showExceptionError(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                "An error has occured: " + e,
                ButtonType.OK
        );
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(icon);
        alert.show();
    }

    private void showWarning(String s) {
        Alert alert = new Alert(Alert.AlertType.WARNING, s, ButtonType.OK);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(icon);
        alert.show();
    }

    public void updateUsers() {
        if (UserProfiles.getSelected() != null && this.user_profiles_choicebox.getItems().contains(UserProfiles.getSelected())) {
            this.user_profiles_choicebox.setValue(UserProfiles.getSelected());
        }
    }

    public void download() {
        try {
            VersionInfo versionInfo = MinecraftApi.getVersionInfo();
            AssetsInfo assetsInfo = MinecraftApi.downloadAssetsInfo(versionInfo);
            FabricMeta meta = FabricApi.getMeta();
            int filesToDownload = 2 +
                    versionInfo.getNumberOfLibrariesToDownload()
                    + assetsInfo.getNumberOfAssetsToDownload()
                    + meta.getNumberOfLibrariesToDownload();


            Thread t = new Thread(new DownloadTask(progress_label, progress_bar, buttonsToDisable, this::showExceptionError, filesToDownload) {
                @Override
                public void action(Downloader downloader) throws Exception {
                    MinecraftApi.downloadFromVersionInfo(versionInfo, downloader);
                    FabricApi.downloadFromMeta(meta, downloader);
                    AuthLibInjectorDownloader.download(downloader);

                    CommandLineArguments arguments = CommandLineArguments.fromVersionInfo(versionInfo).patchFabric(meta).patchAuthLib();
                    Settings.setArguments(arguments);
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionError(e);
        }
    }

    public void downloadModPack() {
        try {
            String latestVersion = ModpackApi.getLatestVersion();
            ModpackMeta meta = ModpackApi.getLatestMeta();
            int filesToDownload = meta._new.length;

            Thread t = new Thread(new DownloadTask(progress_label, progress_bar, buttonsToDisable, this::showExceptionError, filesToDownload) {
                @Override
                public void action(Downloader downloader) throws Exception {
                    ModpackApi.downloadFromMeta(meta, downloader);
                    Settings.setModpackVersion(latestVersion);
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionError(e);
        }
    }

    public void downloadUpdate() {
        try {
            String latestVersion = ModpackApi.getLatestVersion();
            ModpackMeta meta = ModpackApi.getUpdateMeta(Settings.getModpackVersion());
            int filesToDownload = meta._new.length;

            Thread t = new Thread(new DownloadTask(progress_label, progress_bar, buttonsToDisable, this::showExceptionError, filesToDownload) {
                @Override
                public void action(Downloader downloader) throws Exception {
                    ModpackApi.downloadFromMeta(meta, downloader);
                    Settings.setModpackVersion(latestVersion);
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionError(e);
        }
    }

    private void launchMinecraft() {
        if (Settings.getArguments() == null) {
            showWarning("Failed to get JVM/game arguments; Try re-downloading the game");
            return;
        }

        if (user_profiles_choicebox.getValue() == null) {
            showWarning("Сначала выберите пользователя");
            return;
        }

        ((Stage) this.progress_bar.getScene().getWindow()).setIconified(true);
        try {
            System.out.println(Settings.getArguments().format(user_profiles_choicebox.getValue()));
            Process proc = Runtime.getRuntime().exec(
                    Settings.getArguments().format(user_profiles_choicebox.getValue()),
                    new String[0],
                    new File(Settings.getHomePath()));
            InputStream in = proc.getInputStream();
            in.transferTo(System.out);
        } catch (IOException e) {
            showExceptionError(e);
        }
    }

    private void checkForUpdate() {
        if (Settings.getModpackVersion() == null)
            return;

        String latestVersion;
        try {
            latestVersion = ModpackApi.getLatestVersion();
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionError(e);
            return;
        }

        if (Settings.getModpackVersion().equals(latestVersion)) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Новая версия модпака доступна");
        alert.setContentText("Обновить модпак с версии " + Settings.getModpackVersion() + " до " + latestVersion + "?");
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(icon); // JavaFX in a nutshell
        alert.showAndWait().ifPresent((x) -> {
            if (x == ButtonType.YES) {
                downloadUpdate();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonsToDisable = List.of(launch_button, minecraft_button, modpack_button);

        Backgrounds.Background bg = Backgrounds.getRandom();
        root.setBackground(new Background(new BackgroundImage(bg.image, null, null, null, null)));
        title_label.setTextFill(bg.color);
        splash_label.setText(SplashTexts.getRandom());
        splash_label.setTextFill(bg.color);

        user_profiles_choicebox.setItems(UserProfiles.getProfiles());
        updateUsers();
        user_profiles_choicebox.setOnAction((x) -> {
            if (user_profiles_choicebox.getValue() != null)
                UserProfiles.select(user_profiles_choicebox.getValue());
        });

        launch_button.setOnAction((x) -> { launchMinecraft(); });
        minecraft_button.setOnAction((x) -> { download(); });
        modpack_button.setOnAction(x -> { downloadModPack(); });
        settings_button.setOnAction((x) -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("views/settings-view.fxml"));
                loader.setController(new SettingsController(this));
                Stage stage = new Stage();
                Scene scene = new Scene(loader.load());
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Настройки");
                stage.setResizable(false);
                stage.getIcons().add(icon);
                stage.showAndWait();
            } catch (IOException e) {
                showExceptionError(e);
            }
        });

        // Run this later so the main window has time to be drawn
        Platform.runLater(this::checkForUpdate);
    }
}