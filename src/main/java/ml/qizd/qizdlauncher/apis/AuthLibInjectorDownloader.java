package ml.qizd.qizdlauncher.apis;

import ml.qizd.qizdlauncher.Downloader;
import ml.qizd.qizdlauncher.Settings;

import java.net.URL;
import java.nio.file.Path;

public class AuthLibInjectorDownloader {
    private static final String URL = "https://github.com/yushijinhun/authlib-injector/releases/download/v1.2.2/authlib-injector-1.2.2.jar";
    public static final String INJECTOR_FILE = "authlib-injector-1.2.2.jar";

    public static void download(Downloader downloader) throws Exception {
        Downloader.Task task = downloader.taskFrom(new URL(URL), Path.of(Settings.getHomePath(), INJECTOR_FILE));
        downloader.download(task);
    }
}
