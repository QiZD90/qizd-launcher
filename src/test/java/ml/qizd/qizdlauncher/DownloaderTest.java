package ml.qizd.qizdlauncher;

import ml.qizd.qizdlauncher.apis.FabricApi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class DownloaderTest {
    @Test
    public void testDownload() throws IOException, InterruptedException {
        URL url = new URL("http://example.com/");

        Downloader.Callback callback = new Downloader.Callback() {
            @Override
            public void onProgress(Downloader.Task.Result result) {}

            @Override
            public void onCompleted() {}

            @Override
            public void onFailed() {
                System.out.println("Failed");
            }
        };

        Downloader downloader1 = Downloader.Builder
                .create(callback)
                .failBehavior(Downloader.FailBehavior.CANCEL)
                .build();

        Downloader downloader2 = Downloader.Builder
                .create(callback)
                .failBehavior(Downloader.FailBehavior.CANCEL)
                .threads(10)
                .build();

        long start = System.nanoTime();
        FabricApi.downloadFromMeta(FabricApi.getMeta(), downloader1);
        long elapsedTime = System.nanoTime() - start;
        System.out.println(elapsedTime);

        start = System.nanoTime();
        FabricApi.downloadFromMeta(FabricApi.getMeta(), downloader2);
        elapsedTime = System.nanoTime() - start;
        System.out.println(elapsedTime);
    }
}
