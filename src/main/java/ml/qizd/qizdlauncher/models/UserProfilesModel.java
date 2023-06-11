package ml.qizd.qizdlauncher.models;

import ml.qizd.qizdlauncher.users.ElyByUserProfile;
import ml.qizd.qizdlauncher.users.NoAuthUserProfile;

public class UserProfilesModel {
    public static class Profile {
        public String type;
        public String UUID;
        public String username;
        public String accessToken;
    }

    public String selectedUUID;
    public Profile[] profiles;
}
