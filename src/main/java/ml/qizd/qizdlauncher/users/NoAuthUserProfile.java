package ml.qizd.qizdlauncher.users;

public class NoAuthUserProfile implements UserProfile {
    private final String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAuthArgs() {
        return "--username %s --accessToken none".formatted(getName());
    }

    public String toString() {
        return "[NOAUTH] %s".formatted(getName());
    }

    public NoAuthUserProfile(String name) {
        this.name = name;
    }
}
