package ml.qizd.qizdlauncher;

import ml.qizd.qizdlauncher.apis.FabricApi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

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
            public void onFailed() {}
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


        //1853771700
        //903878800
        //2010077000
        //924609000

        long start = System.nanoTime();
        FabricApi.downloadFromMeta(FabricApi.downloadMeta(), downloader1);
        long elapsedTime = System.nanoTime() - start;
        System.out.println(elapsedTime);

        start = System.nanoTime();
        FabricApi.downloadFromMeta(FabricApi.downloadMeta(), downloader1);
        elapsedTime = System.nanoTime() - start;
        System.out.println(elapsedTime);
    }
}
