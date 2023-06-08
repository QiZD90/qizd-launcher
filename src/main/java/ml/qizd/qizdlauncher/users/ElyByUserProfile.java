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

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessToken() {
        return accessToken;
    };

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    @Override
    public String getAuthArgs() {
        return "--accessToken %s --username %s --uuid %s --userType legacy".formatted(name, accessToken, UUID);
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
