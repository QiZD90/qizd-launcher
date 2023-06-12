package ml.qizd.qizdlauncher.users;

public class ElyByUserProfile implements UserProfile {
    public static int ACCESS_TOKEN_LENGTH = 400;
    public static int UUID_LENGTH = 32;

    private final String name;
    private final String accessToken;
    private final String UUID;

    @Override
    public String getName() {
        return name;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUUID() {
        return UUID;
    }


    public String toString() {
        return "[ELY.BY] %s".formatted(getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ElyByUserProfile))
            return false;

        return getUUID().equals(((ElyByUserProfile) obj).getUUID())
                && getName().equals(((ElyByUserProfile) obj).getName())
                && getAccessToken().equals(((ElyByUserProfile) obj).getAccessToken());
    }

    public ElyByUserProfile(String name, String accessToken, String UUID) {
        this.name = name;
        this.accessToken = accessToken;
        this.UUID = UUID;
    }
}
