package ml.qizd.qizdlauncher;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class DownloadTask implements Runnable {
    private Downloader.Callback callback = new Downloader.Callback() {
        @Override
        public void onProgress(Downloader.Task.Result result) {
            System.out.println("DOWNLOAD " + result.filePath);
            filesDownloaded += 1;
            Platform.runLater(() -> {
                progress_bar.setProgress((double) filesDownloaded / filesToDownload);
                progress_label.setText(filesDownloaded + " / " + filesToDownload);
            });
        }

        @Override
        public void onCompleted() {}

        @Override
        public void onFailed(Exception e) {
            Platform.runLater(() -> showExceptionError.accept(e));
        }
    };

    private Downloader downloader = Downloader.Builder
            .create(callback)
            .failBehavior(Downloader.FailBehavior.CANCEL)
            .threads(10)
            .build();

    private final int filesToDownload;
    private int filesDownloaded = 0;
    private final Label progress_label;
    private final ProgressBar progress_bar;
    private final List<Button> buttons;
    private final Consumer<Exception> showExceptionError;

    public abstract void action(Downloader downloader) throws Exception;

    @Override
    public void run() {
        Platform.runLater(() -> {
            progress_label.setText("0 / " + filesToDownload);
            progress_bar.setProgress(0d);
            progress_bar.setVisible(true);
            progress_label.setVisible(true);
            buttons.forEach((x) -> x.setDisable(true));
        });

        try {
            action(downloader);
        } catch (Exception e) {
            callback.onFailed(e);
            e.printStackTrace();
        }

        Platform.runLater(() -> {
            progress_label.setVisible(false);
            progress_bar.setVisible(false);
            buttons.forEach((x) -> x.setDisable(false));
        });
    }

    public DownloadTask(Label progress_label, ProgressBar progress_bar, List<Button> buttons, Consumer<Exception> showExceptionError, int filesToDownload) {
        this.filesToDownload = filesToDownload;
        this.progress_label = progress_label;
        this.progress_bar = progress_bar;
        this.buttons = buttons;
        this.showExceptionError = showExceptionError;
    }
}