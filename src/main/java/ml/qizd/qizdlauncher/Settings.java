package ml.qizd.qizdlauncher;

import ml.qizd.qizdlauncher.users.UserProfile;
import ml.qizd.qizdlauncher.users.UserProfileParser;
import ml.qizd.qizdlauncher.users.UserProfileWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.prefs.*;

public class Settings {
    private static final Preferences prefs = Preferences.userNodeForPackage(Settings.class);
    private static List<UserProfile> profiles;

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

    public static void read() {
        Path userProfiles = Path.of(getHomePath(), "profiles.bin");
        try (FileReader reader = new FileReader(userProfiles.toFile())) {
            profiles = UserProfileParser.parse(reader);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(profiles);
    }

    public static void write() {
        Path userProfiles = Path.of(getHomePath(), "profiles.bin");
        try {
            userProfiles.toFile().createNewFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try (FileWriter writer = new FileWriter(userProfiles.toFile())) {
            UserProfileWriter.write(profiles, writer);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("finished writing");
    }
}
