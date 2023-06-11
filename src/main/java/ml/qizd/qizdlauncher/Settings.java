package ml.qizd.qizdlauncher;

import ml.qizd.qizdlauncher.users.NoAuthUserProfile;
import ml.qizd.qizdlauncher.users.UserProfile;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.*;

public class Settings {
    private static final Preferences prefs = Preferences.userNodeForPackage(Settings.class);
    @Nullable
    private static CommandLineArguments arguments;

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

    public static void read() throws IOException {
        Path args = Path.of(getHomePath(), "arguments.bin");
        if (Files.exists(args)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args.toFile()))) {
                setArguments((CommandLineArguments) ois.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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

        System.out.println("finished writing");
    }
}
