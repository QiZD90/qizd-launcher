package ml.qizd.qizdlauncher.users;


public interface UserProfile {
    String getUUID();
    String getName();
    String getAccessToken();
    String toString();
}
