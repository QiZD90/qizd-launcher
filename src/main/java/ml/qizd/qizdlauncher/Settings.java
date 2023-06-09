package ml.qizd.qizdlauncher;

import ml.qizd.qizdlauncher.users.NoAuthUserProfile;
import ml.qizd.qizdlauncher.users.UserProfile;
import ml.qizd.qizdlauncher.users.UserProfileParser;
import ml.qizd.qizdlauncher.users.UserProfileWriter;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.*;

public class Settings {
    private static final Preferences prefs = Preferences.userNodeForPackage(Settings.class);
    private static List<UserProfile> profiles;
    @Nullable
    private static CommandLineArguments arguments;

    public static void setHomePath(String s) {
        prefs.put("HOME_PATH", s);
    }

    public static String getHomePath() {
        return prefs.get("HOME_PATH", System.getProperty("user.home") + File.separatorChar + "qizd-launcher");
    }

    public static List<UserProfile> getUserProfiles() {
        return profiles;
    }

    public static void addUserProfile(UserProfile profile) {
        profiles.add(profile);
        write();
    }

    public static void clearUserProfiles() {
        profiles.clear();
        write();
    }

    public static void removeUser(UserProfile profile) {
        profiles.remove(profile);
        write();
    }

    @Nullable
    public static CommandLineArguments getArguments() {
        return arguments;
    }

    public static void setArguments(CommandLineArguments args) {
        arguments = args;
        write();
    }

    public static void read() {
        Path userProfiles = Path.of(getHomePath(), "profiles.bin");
        if (!Files.exists(userProfiles)) {
            profiles = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(userProfiles.toFile())) {
            profiles = UserProfileParser.parse(reader);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Path args = Path.of(getHomePath(), "arguments.bin");
        if (Files.exists(args)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args.toFile()))) {
                setArguments((CommandLineArguments) ois.readObject());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println(profiles);
        if (arguments != null) {
            System.out.println(arguments.format(new NoAuthUserProfile("QiZD")));
        }
    }

    public static void write() {
        Path userProfiles = Path.of(getHomePath(), "profiles.bin");
        Path args = Path.of(getHomePath(), "arguments.bin");
        try {
            userProfiles.toFile().createNewFile();
            args.toFile().createNewFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try (FileWriter writer = new FileWriter(userProfiles.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args.toFile()))) {
            UserProfileWriter.write(profiles, writer);
            oos.writeObject(getArguments());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("finished writing");
    }
}
