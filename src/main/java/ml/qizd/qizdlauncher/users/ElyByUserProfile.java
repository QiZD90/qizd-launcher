package ml.qizd.qizdlauncher.users;

public class ElyByUserProfile implements UserProfile {
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

    @Override
    public String getAuthArgs() {
        return "--accessToken %s --username %s --uuid %s --userType legacy".formatted(name, accessToken, UUID);
    }

    public ElyByUserProfile(String name, String accessToken, String UUID) {
        this.name = name;
        this.accessToken = accessToken;
        this.UUID = UUID;
    }
}
