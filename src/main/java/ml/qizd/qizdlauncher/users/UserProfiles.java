package ml.qizd.qizdlauncher.users;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ml.qizd.qizdlauncher.Settings;
import ml.qizd.qizdlauncher.models.UserProfilesModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class UserProfiles {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static List<UserProfile> profiles = new ArrayList<>();
    @Nullable
    private static String selectedUUID = "";

    public static void select(UserProfile profile) {
        selectedUUID = profile.getUUID();
    }

    @Nullable
    public static UserProfile getSelected() throws IllegalArgumentException {
        for (UserProfile profile : profiles)
            if (profile.getUUID().equals(selectedUUID))
                return profile;

        return null;
    }

    public static List<UserProfile> getProfiles() {
        return profiles;
    }

    public static void addUserProfile(@NotNull UserProfile profile) {
        if (profiles.isEmpty())
            select(profile);

        profiles.add(profile);
        try {
            write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void read() throws IOException {
        Path path = Path.of(Settings.getHomePath(), "profiles.json");
        UserProfilesModel model;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            model = gson.fromJson(reader, UserProfilesModel.class);
        }

        profiles.clear();
        for (UserProfilesModel.Profile profile : model.profiles) {
            UserProfile p = switch (profile.type) {
                case "ely" -> new ElyByUserProfile(profile.username, profile.accessToken, profile.UUID);
                default -> new NoAuthUserProfile(profile.username, profile.UUID);
            };
            profiles.add(p);
        }

        selectedUUID = model.selectedUUID;
    }

    public static void write() throws IOException {
        Path path = Path.of(Settings.getHomePath(), "profiles.json");
        UserProfilesModel model = new UserProfilesModel();
        model.selectedUUID = selectedUUID;
        model.profiles = new UserProfilesModel.Profile[profiles.size()];
        ListIterator<UserProfile> it = profiles.listIterator();
        while (it.hasNext()) {
            int index = it.nextIndex();
            UserProfile profile = it.next();
            model.profiles[index] = new UserProfilesModel.Profile();
            model.profiles[index].type = switch (profile) {
                case ElyByUserProfile p -> "ely";
                default -> "no_auth";
            };
            model.profiles[index].UUID = profile.getUUID();
            model.profiles[index].accessToken = profile.getAccessToken();
            model.profiles[index].username = profile.getName();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(gson.toJson(model));
        }
    }
}
