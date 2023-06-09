package ml.qizd.qizdlauncher;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

public class DownloaderTest {
    @Test
    public void testDownload() throws MalformedURLException {
        URL url = new URL("http://example.com/");

        Downloader downloader = new Downloader();
        List<Downloader.Task> tasks = IntStream.range(1, 100)
                .mapToObj(x -> downloader.taskFrom(url, Path.of("F:/test", String.valueOf(x))))
                .toList();

        downloader.downloadAll(tasks, new Downloader.Callback() {
            @Override
            public void onProgress(Downloader.Task.Result result) {
                System.out.println("Downloaded %s %s".formatted(result.filePath, result.status));
            }

            @Override
            public void onCompleted() {
                System.out.println("COMPLETED");
            }
        });
    }
}
