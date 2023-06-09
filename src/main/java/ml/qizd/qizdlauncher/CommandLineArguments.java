package ml.qizd.qizdlauncher;

import ml.qizd.qizdlauncher.apis.AuthLibInjectorDownloader;
import ml.qizd.qizdlauncher.apis.MinecraftDownloader;
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
        args.gameArguments.add("--version " + MinecraftDownloader.MINECRAFT_VERSION);
        args.gameArguments.add("--assetsDir " + "/assets");
        args.gameArguments.add("--gameDir " + Path.of(Settings.getHomePath()));
        args.gameArguments.add("--assetIndex " + "1.19");
        args.mainClass = info.mainClass;

        for (VersionInfo.Library library: info.libraries) {
            if (!library.shouldDownload(MinecraftDownloader.OS_TYPE))
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

        sb.append(profile.getAuthArgs());

        return sb.toString();
    }

    private CommandLineArguments() {}
}
