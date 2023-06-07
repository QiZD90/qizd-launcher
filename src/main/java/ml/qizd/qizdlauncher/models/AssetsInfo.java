package ml.qizd.qizdlauncher.models;

import java.util.Map;

public class AssetsInfo {
    public static class AssetsEntry {
        public String hash;
        public Integer size;
    }

    public Map<String, AssetsEntry> objects;
}
