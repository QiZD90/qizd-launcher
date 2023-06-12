package ml.qizd.qizdlauncher.models;

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
