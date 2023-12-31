package ml.qizd.qizdlauncher;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.*;

public class Settings {
    private static final Preferences prefs = Preferences.userNodeForPackage(Settings.class);
    @Nullable
    private static CommandLineArguments arguments;
    @Nullable
    private static String modpackVersion;

    public static void setHomePath(String s) {
        prefs.put("HOME_PATH", s);
    }

    public static String getHomePath() {
        return prefs.get("HOME_PATH", System.getProperty("user.home") + File.separatorChar + "qizd-launcher");
    }

    @Nullable
    public static CommandLineArguments getArguments() {
        return arguments;
    }

    public static void setArguments(CommandLineArguments args) {
        arguments = args;
        write();
    }

    @Nullable
    public static String getModpackVersion() {
        return modpackVersion;
    }

    public static void setModpackVersion(String modpackVersion) {
        Settings.modpackVersion = modpackVersion;
        write();
    }

    public static void read() throws IOException {
        Path args = Path.of(getHomePath(), "arguments.bin");
        if (Files.exists(args)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args.toFile()))) {
                setArguments((CommandLineArguments) ois.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        Path version = Path.of(getHomePath(), "modpack.version");
        if (Files.exists(version)) {
            try (BufferedReader reader = Files.newBufferedReader(version)) {
                modpackVersion = reader.readLine();
            }
        }
    }

    public static void write() {
        Path args = Path.of(getHomePath(), "arguments.bin");
        try {
            args.toFile().createNewFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args.toFile()))) {
            oos.writeObject(getArguments());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (getModpackVersion() != null) {
            try {
                Path version = Path.of(getHomePath(), "modpack.version");
                version.toFile().createNewFile();
                try (BufferedWriter writer = Files.newBufferedWriter(version)) {
                    writer.write(getModpackVersion());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println("finished writing");
    }
}
