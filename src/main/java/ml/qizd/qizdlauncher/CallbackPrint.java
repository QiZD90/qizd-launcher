package ml.qizd.qizdlauncher;

public class CallbackPrint extends Downloader.Callback {
    @Override
    public void onProgress(Downloader.Task.Result result) {
        System.out.println("DOWNLOADED %s %s".formatted(result.filePath, result.status));
    }

    @Override
    public void onCompleted() {
        System.out.println("COMPLETE");
    }
}
