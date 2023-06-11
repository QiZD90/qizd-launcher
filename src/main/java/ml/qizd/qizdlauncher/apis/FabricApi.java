package ml.qizd.qizdlauncher.apis;

import com.google.gson.Gson;
import ml.qizd.qizdlauncher.Downloader;
import ml.qizd.qizdlauncher.Settings;
import ml.qizd.qizdlauncher.models.FabricMeta;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FabricApi {
    private static String API_URL = "https://meta.fabricmc.net";
    public static String LOADER_VERSION = "0.14.19";
    private static Gson gson = new Gson();
    private static OkHttpClient client = new OkHttpClient();

    public static FabricMeta downloadMeta() throws IOException {
        Request request = new Request.Builder()
                .url(API_URL + "/v2/versions/loader/%s/%s/profile/json".formatted(MinecraftApi.MINECRAFT_VERSION, LOADER_VERSION))
                .build();

        try (Response response = client.newCall(request).execute()) {
            // TODO: handle non-successful call
            return gson.fromJson(response.body().charStream(), FabricMeta.class);
        }
    }

    private static void downloadLibraries(FabricMeta meta, Downloader downloader) throws IOException {
        List<Downloader.Task> tasks = new ArrayList<>();

        for (FabricMeta.Library library : meta.libraries) {
            tasks.add(downloader.taskFrom(
                    new URL(library.url + library.getPath()),
                    Path.of(Settings.getHomePath(), "libraries/", library.getPath())
            ));
        }

        downloader.downloadAll(tasks);
    }

    public static void downloadFromMeta(FabricMeta meta, Downloader downloader) throws IOException {
        downloadLibraries(meta, downloader);
    }
}