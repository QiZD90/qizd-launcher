package ml.qizd.qizdlauncher.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ml.qizd.qizdlauncher.*;
import ml.qizd.qizdlauncher.apis.AuthLibInjectorDownloader;
import ml.qizd.qizdlauncher.apis.FabricApi;
import ml.qizd.qizdlauncher.apis.MinecraftApi;
import ml.qizd.qizdlauncher.models.AssetsInfo;
import ml.qizd.qizdlauncher.models.FabricMeta;
import ml.qizd.qizdlauncher.models.VersionInfo;
import ml.qizd.qizdlauncher.users.UserProfile;
import ml.qizd.qizdlauncher.users.UserProfiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static class Box<T> {
        public T value;
        public Box(T t) { this.value = t; }
    }

    @FXML
    private Label title_label;
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

    private void showExceptionError(Exception e) {
        // TODO: polish this
        e.printStackTrace();
        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                "An error has occured: " + e.toString(),
                ButtonType.OK
        );
        alert.show();
    }

    private void showWarning(String s) {
        // TODO: polish this
        Alert alert = new Alert(Alert.AlertType.WARNING, s, ButtonType.OK);
        alert.show();
    }

    private void buttonSetEnabled(boolean enabled) {
        launch_button.setDisable(!enabled);
        minecraft_button.setDisable(!enabled);
        modpack_button.setDisable(!enabled);
    }

    private String formatProgressText(int a, int b) {
        return String.format("%d / %d", a, b);
    }

    public void updateUsers() {
        if (UserProfiles.getSelected() != null && this.user_profiles_choicebox.getItems().contains(UserProfiles.getSelected())) {
            this.user_profiles_choicebox.setValue(UserProfiles.getSelected());
        }
    }

    public void download() {
        progress_bar.setVisible(true);
        progress_label.setVisible(true);
        progress_label.setText(formatProgressText(0, 0));
        buttonSetEnabled(false);

        final Box<Integer> filesToDownload = new Box<>(2); // client.jar and authlib
        final Box<Integer> filesDownloaded = new Box<>(0);

        try {
            VersionInfo versionInfo = MinecraftApi.getVersionInfo();
            AssetsInfo assetsInfo = MinecraftApi.downloadAssetsInfo(versionInfo);
            FabricMeta meta = FabricApi.getMeta();
            filesToDownload.value +=
                    versionInfo.getNumberOfLibrariesToDownload()
                            + assetsInfo.getNumberOfAssetsToDownload()
                            + meta.getNumberOfLibrariesToDownload();

            progress_label.setText(formatProgressText(filesDownloaded.value, filesToDownload.value));

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Downloader.Callback callback = new Downloader.Callback() {
                        @Override
                        public void onProgress(Downloader.Task.Result result) {
                            System.out.printf("DOWNLOAD %s\n", result.filePath);

                            filesDownloaded.value += 1;
                            progress_bar.setProgress((double) filesDownloaded.value / filesToDownload.value);
                            updateMessage(formatProgressText(filesDownloaded.value, filesToDownload.value));
                            Platform.runLater(() -> progress_label.setText(getMessage()));
                        }

                        @Override
                        public void onCompleted() {
                            if (filesDownloaded.value.equals(filesToDownload.value))
                                return;

                            progress_bar.setVisible(false);
                            progress_label.setVisible(false);
                            buttonSetEnabled(true);
                            CommandLineArguments args = CommandLineArguments.fromVersionInfo(versionInfo).patchFabric(meta).patchAuthLib();
                            Settings.setArguments(args);
                        }

                        @Override
                        public void onFailed() {
                            System.out.println("FAIL");//showWarning("Failed to download some file; Chaos ensues"); // TODO: implement this
                        }
                    };

                    Downloader downloader = Downloader.Builder
                            .create(callback)
                            .failBehavior(Downloader.FailBehavior.CANCEL)
                            .threads(10)
                            .build();

                    MinecraftApi.downloadFromVersionInfo(versionInfo, downloader);
                    FabricApi.downloadFromMeta(meta, downloader);
                    AuthLibInjectorDownloader.download(downloader);

                    CommandLineArguments arguments = CommandLineArguments.fromVersionInfo(versionInfo).patchFabric(meta).patchAuthLib();
                    Settings.setArguments(arguments);
                    return null;
                }
            };

            new Thread(task).start();
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
            Process proc = Runtime.getRuntime().exec(
                    Settings.getArguments().format(user_profiles_choicebox.getValue()),
                    new String[0],
                    new File(Settings.getHomePath())
            );
            InputStream in = proc.getInputStream();
            in.transferTo(System.out);
        } catch (IOException e) {
            showExceptionError(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        title_label.setText(TitleTexts.getRandom());
        user_profiles_choicebox.setItems(UserProfiles.getProfiles());
        updateUsers();
        user_profiles_choicebox.setOnAction((x) -> {
            if (user_profiles_choicebox.getValue() != null)
                UserProfiles.select(user_profiles_choicebox.getValue());
        });

        launch_button.setOnAction((x) -> { launchMinecraft(); });
        minecraft_button.setOnAction((x) -> { download(); });
        settings_button.setOnAction((x) -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("views/settings-view.fxml"));
                loader.setController(new SettingsController(this));
                Stage stage = new Stage();
                Scene scene = new Scene(loader.load());
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                showExceptionError(e);
            }
        });
    }
}