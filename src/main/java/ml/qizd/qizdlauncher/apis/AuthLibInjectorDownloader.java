package ml.qizd.qizdlauncher.apis;

import ml.qizd.qizdlauncher.Settings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class AuthLibInjectorDownloader {
    private static final String URL = "https://github.com/yushijinhun/authlib-injector/releases/download/v1.2.2/authlib-injector-1.2.2.jar";
    public static final String INJECTOR_FILE = "authlib-injector-1.2.2.jar";
    private static final OkHttpClient client = new OkHttpClient();
    public static void download() throws Exception {
        Request request = new Request.Builder()
                .url(URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("An error occured while trying to download authlib injector");

            Path path = Path.of(Settings.getHomePath(), INJECTOR_FILE);
            path.getParent().toFile().mkdirs();
            Files.write(path, response.body().bytes(), StandardOpenOption.CREATE);
        }
    }
}
