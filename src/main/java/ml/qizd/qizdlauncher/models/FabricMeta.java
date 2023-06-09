package ml.qizd.qizdlauncher.models;

public class FabricMeta {
    public static class Arguments {
        public String[] game;
        public String[] jvm;
    }

    public static class Library {
        String name;
        public String url;

        public String getPath() {
            String[] parts = name.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid artifact name");
            }

            String[] domain = parts[0].split("\\.");
            StringBuilder sb = new StringBuilder();
            for (String s : domain) {
                sb.append(s);
                sb.append("/");
            }

            sb.append(parts[1]);
            sb.append("/");
            sb.append(parts[2]);
            sb.append("/");
            sb.append("%s-%s.jar".formatted(parts[1], parts[2]));

            return sb.toString();
        }
    }

    public String mainClass;
    public Arguments arguments;
    public Library[] libraries;
}
