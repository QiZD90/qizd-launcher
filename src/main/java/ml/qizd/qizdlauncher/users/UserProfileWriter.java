package ml.qizd.qizdlauncher.users;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class UserProfileWriter {
    private static void writeNoAuth(NoAuthUserProfile profile, FileWriter writer) throws IOException {
        writer.write(profile.getName().length());
        writer.write(profile.getName());
    }

    private static void writeElyBy(ElyByUserProfile profile, FileWriter writer) throws IOException {
        writer.write(profile.getName().length());
        writer.write(profile.getName());
        writer.write(profile.getAccessToken());
        writer.write(profile.getUUID());
    }

    public static void write(List<UserProfile> profiles, FileWriter writer) throws IOException {
        for (UserProfile profile : profiles) {
            if (profile instanceof NoAuthUserProfile) {
                writer.write(0);
                writeNoAuth((NoAuthUserProfile) profile, writer);
            } else if (profile instanceof ElyByUserProfile) {
                writer.write(1);
                writeElyBy((ElyByUserProfile) profile, writer);
            }
        }
    }
}
