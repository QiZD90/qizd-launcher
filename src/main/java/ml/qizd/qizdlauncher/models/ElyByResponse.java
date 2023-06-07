package ml.qizd.qizdlauncher.models;

public class ElyByResponse {
    public static class User {
        public String id;
        public String username;
    }

    public String accessToken;
    public String error;
    public String errorMessage;
    public User user;
}
