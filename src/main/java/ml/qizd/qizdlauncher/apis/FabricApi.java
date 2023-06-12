package ml.qizd.qizdlauncher.apis;

import com.google.gson.Gson;
import ml.qizd.qizdlauncher.Downloader;
import ml.qizd.qizdlauncher.Settings;
import ml.qizd.qizdlauncher.models.FabricMeta;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricApi {
    private static final String API_URL = "https://meta.fabricmc.net";
    public static final String LOADER_VERSION = "0.14.19";
    private static final Gson gson = new Gson();
    private static final OkHttpClient client = new OkHttpClient();

    public static FabricMeta getMeta() throws IOException {
        Request request = new Request.Builder()
                .url(API_URL + "/v2/versions/loader/%s/%s/profile/json".formatted(MinecraftApi.MINECRAFT_VERSION, LOADER_VERSION))
                .build();

        try (Response response = client.newCall(request).execute(); ResponseBody body = response.body()) {
            if (body == null || !response.isSuccessful())
                throw new IOException("Failed to get fabric meta");

            return gson.fromJson(body.charStream(), FabricMeta.class);
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