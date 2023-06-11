package ml.qizd.qizdlauncher.users;

public class ElyByUserProfile implements UserProfile {
    public static int ACCESS_TOKEN_LENGTH = 400;
    public static int UUID_LENGTH = 32;

    private String name;
    private String accessToken;
    private String UUID;

    @Override
    public String getName() {
        return name;
    }

    public String getAccessToken() {
        return accessToken;
    };

    public String getUUID() {
        return UUID;
    }


    public String toString() {
        return "[ELY.BY] %s".formatted(getName());
    }

    public ElyByUserProfile(String name, String accessToken, String UUID) {
        this.name = name;
        this.accessToken = accessToken;
        this.UUID = UUID;
    }
}
