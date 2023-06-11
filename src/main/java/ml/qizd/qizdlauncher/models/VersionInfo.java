package ml.qizd.qizdlauncher.models;

import ml.qizd.qizdlauncher.apis.MinecraftApi;

public class VersionInfo {
    public static class AssetIndex {
        public String id;
        public String sha1;
        public Integer size;
        public Integer totalSize;
        public String url;
    }

    public static class Downloads {
        public static class DownloadsEntries {
            public String sha1;
            public Integer size;
            public String url;
        }

        public DownloadsEntries client;
        public DownloadsEntries server;
    }

    public static class Library {
        public static class Downloads {
            public static class Artifact {
                public String path;
                public String sha1;
                public Integer size;
                public String url;
            }

            public Artifact artifact;
        }

        public static class Rule {
            public static class OS {
                public String name;
            }

            public String action;
            public OS os;
        }

        public Downloads downloads;
        public String name;
        public Rule[] rules;

        public boolean shouldDownload(String os) {
            if (this.rules == null || this.rules.length == 0)
                return true;

            return rules[0].action.equals("allow") && rules[0].os.name.equals(os);
        }
    }

    public int getNumberOfLibrariesToDownload() {
        int number = 0;
        for (Library library : libraries) {
            if (library.shouldDownload(MinecraftApi.OS_TYPE))
                number++;
        }

        return number;
    }

    public AssetIndex assetIndex;
    public String assets;
    public Downloads downloads;
    public Library[] libraries;
    public String mainClass;
}
