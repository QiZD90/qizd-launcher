package ml.qizd.qizdlauncher.apis;

import com.google.gson.Gson;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MinecraftApi {
    public static final String OS_TYPE = "windows"; // TODO: refactor
    public static final String MINECRAFT_VERSION = "1.19.2";

    private static final String VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String DOWNLOAD_URL = "https://resources.download.minecraft.net/";

    private static final Gson gson = new Gson();
    private static final OkHttpClient client = new OkHttpClient();
    private static String downloadingFile = "";

    public static String getDownloadingFileName() {
        return downloadingFile;
    }

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

    private static void downloadClientJar(VersionInfo versionInfo) throws Exception {
        Request request = new Request.Builder()
                .url(versionInfo.downloads.client.url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("An error occured while trying to download client jar");

            downloadingFile = "client.jar size: " + versionInfo.downloads.client.size.toString();
            new File(Settings.getHomePath()).mkdirs();
            Files.write(Path.of(Settings.getHomePath(), "client.jar"), response.body().bytes(), StandardOpenOption.CREATE);
        }
    }

    private static void downloadLibraries(VersionInfo versionInfo) throws Exception {
        for (VersionInfo.Library library : versionInfo.libraries) {
            if (!library.shouldDownload(OS_TYPE))
                continue;

            Request request = new Request.Builder()
                    .url(library.downloads.artifact.url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null)
                    throw new IOException("An error occured while trying to download library " + library.name);

                downloadingFile = "libraries/" + library.downloads.artifact.path + " size: " + library.downloads.artifact.size;
                Path path = Path.of(Settings.getHomePath(), "libraries", library.downloads.artifact.path);
                path.getParent().toFile().mkdirs();
                Files.write(path, response.body().bytes(), StandardOpenOption.CREATE);
            }
        }
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

    private static void downloadAssets(AssetsInfo assetsInfo) throws Exception {
        for (AssetsInfo.AssetsEntry entry : assetsInfo.objects.values()) {
            Request request = new Request.Builder()
                    .url(DOWNLOAD_URL + entry.hash.substring(0, 2) + "/" + entry.hash)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null)
                    throw new IOException("An error occured while trying to download assets " + entry.hash);

                downloadingFile = "assets/objects/" + entry.hash.substring(0, 2) + "/" + entry.hash;
                Path path = Path.of(Settings.getHomePath(), "assets", "objects", entry.hash.substring(0, 2), entry.hash);
                path.getParent().toFile().mkdirs();
                Files.write(path, response.body().bytes(), StandardOpenOption.CREATE);
            }
        }
    }

    public static VersionInfo download() throws Exception {
        VersionInfo versionInfo = getVersionInfo();
        downloadClientJar(versionInfo);
        downloadLibraries(versionInfo);
        AssetsInfo assetsInfo = downloadAssetsInfo(versionInfo);
        downloadAssets(assetsInfo);

        return versionInfo;
    }
}
