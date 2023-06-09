package ml.qizd.qizdlauncher;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Downloader {
    public class Task implements Callable<Task.Result> {
        public static class Result {
            public enum Status {
                OK, NOT_OK
            }
            String filePath;
            Status status;
            public Result(String filePath, Status status) {
                this.filePath = filePath;
                this.status = status;
            }
        }

        Request request;
        Path filePath;

        @Override
        public Result call() {
            try (Response response = client.newCall(request).execute()) {
                ResponseBody body;
                if (!response.isSuccessful() || (body = response.body()) == null)
                    return new Result(this.filePath.toString(), Result.Status.NOT_OK);

                filePath.getParent().toFile().mkdirs();
                try (OutputStream stream = Files.newOutputStream(filePath)){
                    body.byteStream().transferTo(stream);
                }
            } catch (Exception e) {
                return new Result(this.filePath.toString(), Result.Status.NOT_OK);
            }

            return new Result(this.filePath.toString(), Result.Status.OK);
        }

        private Task(URL url, Path filePath) {
            this.request = new Request.Builder().url(url).build();
            this.filePath = filePath;
        }
    }

    public static abstract class Callback {
        public abstract void onProgress(Task.Result result);
        public abstract void onCompleted();
    }

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private OkHttpClient client = new OkHttpClient();

    public void download(Downloader.Task task, @Nullable Downloader.Callback callback) {
        try {
            threadPool.submit(task).get();
            if (callback != null) {
                callback.onCompleted();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadAll(List<Downloader.Task> tasks, @Nullable Downloader.Callback callback) {
        try {
            List<Future<Task.Result>> futures = threadPool.invokeAll(tasks);
            for (Future<Task.Result> future : futures) {
                Task.Result result = future.get();
                if (callback != null)
                    callback.onProgress(result);
            }

            if (callback != null)
                callback.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Task taskFrom(URL url, Path path) {
        return new Task(url, path);
    }
}
