package ml.qizd.qizdlauncher;

import ml.qizd.qizdlauncher.apis.FabricApi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class DownloaderTest {
    @Test
    public void testDownload() throws IOException, InterruptedException {
        URL url = new URL("http://invalid-link.com/");
        URL url2 = new URL("http://example.com/");

        Downloader.Callback callback = new Downloader.Callback() {
            @Override
            public void onProgress(Downloader.Task.Result result) {
                System.out.println(result);
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed");
            }

            @Override
            public void onFailed() {
                System.out.println("Failed");
            }
        };

        Downloader downloader = Downloader.Builder
                .create(callback)
                .failBehavior(Downloader.FailBehavior.CANCEL)
                .build();

        List<Downloader.Task> tasks = new ArrayList<>(IntStream.range(0, 100).mapToObj((x) -> downloader.taskFrom(url, Path.of("F:", "test", String.valueOf(x)))).toList());
        tasks.add(downloader.taskFrom(url2, Path.of("F:", "test", "example")));
        downloader.downloadAll(tasks);
    }
}
