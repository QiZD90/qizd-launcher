package ml.qizd.qizdlauncher;

import ml.qizd.qizdlauncher.apis.AuthLibInjectorDownloader;
import ml.qizd.qizdlauncher.apis.MinecraftApi;
import ml.qizd.qizdlauncher.models.FabricMeta;
import ml.qizd.qizdlauncher.models.VersionInfo;
import ml.qizd.qizdlauncher.users.UserProfile;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class CommandLineArguments implements Serializable {
    String mainClass = "";
    Set<String> gameArguments = new HashSet<>();
    Set<String> jvmArguments = new HashSet<>();
    Set<String> classPaths = new HashSet<>();

    public static CommandLineArguments fromVersionInfo(VersionInfo info) {
        CommandLineArguments args = new CommandLineArguments();
        args.gameArguments.add("--version " + MinecraftApi.MINECRAFT_VERSION);
        args.gameArguments.add("--assetsDir " + Path.of(Settings.getHomePath(), "assets"));
        args.gameArguments.add("--gameDir " + Path.of(Settings.getHomePath()));
        args.gameArguments.add("--assetIndex " + "1.19");
        args.mainClass = info.mainClass;

        for (VersionInfo.Library library: info.libraries) {
            if (!library.shouldDownload(MinecraftApi.OS_TYPE))
                continue;

            args.classPaths.add("libraries/%s".formatted(library.downloads.artifact.path));
        }

        return args;
    }

    public CommandLineArguments patchFabric(FabricMeta meta) {
        gameArguments.add(meta.arguments.jvm[0]);
        for (FabricMeta.Library library: meta.libraries) {
            this.classPaths.add("libraries/%s".formatted(library.getPath()));
        }

        mainClass = meta.mainClass;

        return this;
    }

    public CommandLineArguments patchAuthLib() {
        jvmArguments.add("-javaagent:%s=ely.by".formatted(AuthLibInjectorDownloader.INJECTOR_FILE));

        return this;
    }

    public String format(UserProfile profile) {
        StringBuilder sb = new StringBuilder("java -cp ");
        for (String classPath: classPaths) {
            sb.append(classPath);
            sb.append(";");
        }
        sb.append(Settings.getHomePath() + "/client.jar;");
        sb.append(" ");

        for (String arg : jvmArguments) {
            sb.append(arg);
            sb.append(" ");
        }

        sb.append(mainClass);
        sb.append(" ");

        for (String arg: gameArguments) {
            sb.append(arg);
            sb.append(" ");
        }

        sb.append("--accessToken ");
        sb.append(profile.getAccessToken());
        sb.append(" --username ");
        sb.append(profile.getName());
        sb.append(" --uuid ");
        sb.append(profile.getUUID());
        sb.append(" --userType legacy");

        return sb.toString();
    }

    private CommandLineArguments() {}
}
