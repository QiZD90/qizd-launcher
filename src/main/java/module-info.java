module ml.qizd.qizdlauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires okhttp3;
    requires java.prefs;


    opens ml.qizd.qizdlauncher to javafx.fxml;
    opens ml.qizd.qizdlauncher.models to com.google.gson;
    exports ml.qizd.qizdlauncher;
    exports ml.qizd.qizdlauncher.apis;
    opens ml.qizd.qizdlauncher.apis to javafx.fxml;
    exports ml.qizd.qizdlauncher.users;
    opens ml.qizd.qizdlauncher.users to javafx.fxml;
}