package ml.qizd.qizdlauncher.apis;

import com.google.gson.Gson;
import ml.qizd.qizdlauncher.Settings;
import ml.qizd.qizdlauncher.models.FabricMeta;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricApi {
    private static String API_URL = "https://meta.fabricmc.net";
    public static String LOADER_VERSION = "0.14.19";
    private static Gson gson = new Gson();
    private static OkHttpClient client = new OkHttpClient();

    private static FabricMeta downloadMeta() throws IOException {
        Request request = new Request.Builder()
                .url(API_URL + "/v2/versions/loader/%s/%s/profile/json".formatted(MinecraftDownloader.MINECRAFT_VERSION, LOADER_VERSION))
                .build();

        try (Response response = client.newCall(request).execute()) {
            // TODO: handle non-successful call
            return gson.fromJson(response.body().charStream(), FabricMeta.class);
        }
    }

    private static void downloadLibraries(FabricMeta meta) throws IOException {
        for (FabricMeta.Library library : meta.libraries) {
            Request request = new Request.Builder()
                    .url(library.url + library.getPath())
                    .build();

            try (Response response = client.newCall(request).execute()) {
                // TODO: hande non-successful call
                Path path = Path.of(Settings.getHomePath(), "libraries/", library.getPath());
                path.getParent().toFile().mkdirs();
                Files.write(path, response.body().bytes());
            }
        }
    }

    public static FabricMeta download() throws IOException {
        FabricMeta meta = downloadMeta();
        downloadLibraries(meta);

        return meta;
    }
}