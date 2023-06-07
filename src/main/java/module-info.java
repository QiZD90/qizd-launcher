module ml.qizd.qizdlauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires okhttp3;


    opens ml.qizd.qizdlauncher to javafx.fxml;
    opens ml.qizd.qizdlauncher.models to com.google.gson;
    exports ml.qizd.qizdlauncher;
}