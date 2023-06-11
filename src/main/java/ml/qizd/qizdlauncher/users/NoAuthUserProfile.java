package ml.qizd.qizdlauncher.users;

import java.util.UUID;

public class NoAuthUserProfile implements UserProfile {
    private final String name;
    private final UUID uuid;

    @Override
    public String getUUID() {
        return uuid.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAccessToken() {
        return "none";
    }

    public String toString() {
        return "[NOAUTH] %s".formatted(getName());
    }

    public NoAuthUserProfile(String name, String uuid) {
        this.name = name;
        this.uuid = UUID.fromString(uuid);
    }

    public NoAuthUserProfile(String name) {
        this.name = name;
        this.uuid = UUID.randomUUID();
    }
}
