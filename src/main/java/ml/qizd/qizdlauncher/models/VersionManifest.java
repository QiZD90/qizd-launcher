package ml.qizd.qizdlauncher.models;

public class VersionManifest {
    public static class LatestVersions {
        public String release;
        public String snapshot;
    }

    public LatestVersions latest;

    public VersionManifestEntry[] versions;
}
