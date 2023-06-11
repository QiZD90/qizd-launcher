package ml.qizd.qizdlauncher;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

public class DownloaderTest {
    @Test
    public void testDownload() throws MalformedURLException, InterruptedException {
        URL url = new URL("http://example.com/");

        Downloader downloader = Downloader.Builder
                .create(new Downloader.Callback() {
                    @Override
                    public void onProgress(Downloader.Task.Result result) {
                        System.out.println("DOWNLOADED " + result.filePath);
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("COMPLETED");
                    }

                    @Override
                    public void onFailed() {
                        System.out.println("FAILED");
                    }
                })
                .failBehavior(Downloader.FailBehavior.CANCEL)
                .build();

        List<Downloader.Task> tasks = IntStream.range(1, 100)
                .mapToObj(x -> downloader.taskFrom(url, Path.of("F:/test", String.valueOf(x))))
                .toList();

        downloader.downloadAll(tasks);
        System.out.println("END");
    }
}
