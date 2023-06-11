package ml.qizd.qizdlauncher.users;


public interface UserProfile {
    public String getUUID();
    public String getName();
    public String getAccessToken();
    public String toString();
}
