package ml.qizd.qizdlauncher.models;

public class VersionManifest {
    public static class LatestVersions {
        public String release;
        public String snapshot;
    }

    public static class Entry {
        public String id;
        public String url;
    }

    public LatestVersions latest;

    public Entry[] versions;
}
