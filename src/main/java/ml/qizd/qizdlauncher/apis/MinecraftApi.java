package ml.qizd.qizdlauncher.apis;

import com.google.gson.Gson;
import ml.qizd.qizdlauncher.CallbackPrint;
import ml.qizd.qizdlauncher.Downloader;
import ml.qizd.qizdlauncher.Settings;
import ml.qizd.qizdlauncher.models.AssetsInfo;
import ml.qizd.qizdlauncher.models.VersionInfo;
import ml.qizd.qizdlauncher.models.VersionManifest;
import ml.qizd.qizdlauncher.models.VersionManifestEntry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class MinecraftApi {
    public static final String OS_TYPE = "windows"; // TODO: refactor
    public static final String MINECRAFT_VERSION = "1.19.2";

    private static final String VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String DOWNLOAD_URL = "https://resources.download.minecraft.net/";

    private static final Gson gson = new Gson();
    private static final OkHttpClient client = new OkHttpClient();

    private static String getVersionURL() throws Exception {
        Request request = new Request.Builder()
                .url(VERSION_MANIFEST)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("An error occurred while trying to get version manifest");

            VersionManifest manifest = gson.fromJson(response.body().charStream(), VersionManifest.class);
            for (VersionManifestEntry version : manifest.versions) {
                if (version.id.equals(MINECRAFT_VERSION))
                    return version.url;
            }
        }

        return null;
    }

    public static VersionInfo getVersionInfo() throws Exception {
        Request request = new Request.Builder()
                .url(getVersionURL())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("An error occured while trying to get version info");

            return gson.fromJson(response.body().charStream(), VersionInfo.class);
        }
    }

    private static void downloadClientJar(VersionInfo versionInfo, Downloader downloader) throws Exception {
        Downloader.Task task = downloader.taskFrom(
                new URL(versionInfo.downloads.client.url),
                Path.of(Settings.getHomePath(), "client.jar")
        );
        downloader.download(task, new CallbackPrint());
    }

    private static void downloadLibraries(VersionInfo versionInfo, Downloader downloader) throws Exception {
        List<Downloader.Task> tasks = new ArrayList<>();
        for (VersionInfo.Library library : versionInfo.libraries) {
            if (!library.shouldDownload(OS_TYPE))
                continue;

            tasks.add(downloader.taskFrom(
                    new URL(library.downloads.artifact.url),
                    Path.of(Settings.getHomePath(), "libraries", library.downloads.artifact.path)
            ));
        }

        downloader.downloadAll(tasks,  new CallbackPrint());
    }

    private static AssetsInfo downloadAssetsInfo(VersionInfo versionInfo) throws Exception {
        Request request = new Request.Builder()
                .url(versionInfo.assetIndex.url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("An error occured while trying to get assets info");

            Path path = Path.of(Settings.getHomePath(), "assets", "indexes", versionInfo.assets + ".json");
            path.getParent().toFile().mkdirs();
            byte[] bytes = response.body().bytes();
            Files.write(path, bytes, StandardOpenOption.CREATE);

            return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), AssetsInfo.class);
        }
    }

    private static void downloadAssets(AssetsInfo assetsInfo, Downloader downloader) throws Exception {
        List<Downloader.Task> tasks = new ArrayList<>();

        for (AssetsInfo.AssetsEntry entry : assetsInfo.objects.values()) {
            tasks.add(downloader.taskFrom(
                    new URL(DOWNLOAD_URL + entry.hash.substring(0, 2) + "/" + entry.hash),
                    Path.of(Settings.getHomePath(), "assets", "objects", entry.hash.substring(0, 2), entry.hash)
            ));
        }

        downloader.downloadAll(tasks, new CallbackPrint());
    }

    public static VersionInfo download() throws Exception {
        VersionInfo versionInfo = getVersionInfo();
        Downloader downloader = new Downloader();
        downloadClientJar(versionInfo, downloader);
        downloadLibraries(versionInfo, downloader);
        AssetsInfo assetsInfo = downloadAssetsInfo(versionInfo);
        downloadAssets(assetsInfo, downloader);

        return versionInfo;
    }
}
