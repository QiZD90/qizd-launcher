package ml.qizd.qizdlauncher.apis;

import com.google.gson.Gson;
import ml.qizd.qizdlauncher.Downloader;
import ml.qizd.qizdlauncher.Settings;
import ml.qizd.qizdlauncher.models.ModpackLatestVersion;
import ml.qizd.qizdlauncher.models.ModpackMeta;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ModpackApi {
    private static final String API_URL = "http://188.120.226.32:8080";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static String getLatestVersion() throws IOException {
        Request request = new Request.Builder()
                .url(API_URL + "/latest_version")
                .build();

        try (Response response = client.newCall(request).execute(); ResponseBody body = response.body()) {
            if (!response.isSuccessful() || body == null)
                throw new IOException();

            return gson.fromJson(body.charStream(), ModpackLatestVersion.class).latest_version;
        }
    }

    public static ModpackMeta getLatestMeta() throws IOException {
        Request request = new Request.Builder()
                .url(API_URL + "/latest")
                .build();

        try (Response response = client.newCall(request).execute(); ResponseBody body = response.body()) {
            if (!response.isSuccessful() || body == null)
                throw new IOException();

            return gson.fromJson(body.charStream(), ModpackMeta.class);
        }
    }

    public static ModpackMeta getUpdateMeta(String fromVersion) throws IOException {
        Request request = new Request.Builder()
                .url(API_URL + "/update/" + fromVersion)
                .build();

        try (Response response = client.newCall(request).execute(); ResponseBody body = response.body()) {
            if (!response.isSuccessful() || body == null)
                throw new IOException();

            return gson.fromJson(body.charStream(), ModpackMeta.class);
        }
    }

    private static void removeObsoleteMods(ModpackMeta meta) throws IOException {
        for (String removed: meta.removed) {
            Path path = Path.of(Settings.getHomePath(), "mods", removed);
            Files.deleteIfExists(path);
        }
    }

    private static void downloadMods(ModpackMeta meta, Downloader downloader) throws MalformedURLException{
        List<Downloader.Task> tasks = new ArrayList<>();
        for (String file : meta._new) {
            tasks.add(downloader.taskFrom(
                    new URL(API_URL + "/file/" + file),
                    Path.of(Settings.getHomePath(), "mods", file)));
        }

        downloader.downloadAll(tasks);
    }

    public static void downloadFromMeta(ModpackMeta meta, Downloader downloader) throws IOException {
        removeObsoleteMods(meta);
        downloadMods(meta, downloader);
    }
}
