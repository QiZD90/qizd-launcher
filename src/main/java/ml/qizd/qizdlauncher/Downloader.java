package ml.qizd.qizdlauncher;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Downloader {
    public class Task implements Callable<Task.Result> {
        public static class Result {
            public String filePath;
            public Result(String filePath) {
                this.filePath = filePath;
            }
        }

        Request request;
        Path filePath;

        public Result call() throws IOException {
            try (Response response = client.newCall(request).execute()) {
                ResponseBody body;
                if (!response.isSuccessful() || (body = response.body()) == null)
                    return new Result(this.filePath.toString());

                filePath.getParent().toFile().mkdirs();
                try (OutputStream stream = Files.newOutputStream(filePath)){
                    body.byteStream().transferTo(stream);
                }

                return new Result(filePath.toString());
            }
        }

        private Task(URL url, Path filePath) {
            this.request = new Request.Builder().url(url).build();
            this.filePath = filePath;
        }
    }

    public static abstract class Callback {
        public abstract void onProgress(Task.Result result);
        public abstract void onCompleted();
        public abstract void onFailed();
    }

    public enum FailBehavior { // TODO: implement
        CANCEL, // Cancels the rest of the downloads on first error
        IGNORE // Reports the error and continues the download
    }

    public static class Builder {
        Callback callback;
        FailBehavior failBehavior = FailBehavior.CANCEL;

        public Builder failBehavior(FailBehavior failBehavior) {
            this.failBehavior = failBehavior;
            return this;
        }

        public static Builder create(Callback callback) {
            Builder builder = new Builder();
            builder.callback = callback;
            return builder;
        }

        public Downloader build() {
            Downloader downloader = new Downloader();
            downloader.callback = callback;
            downloader.failBehavior = failBehavior;

            return downloader;
        }

        private Builder() {}
    }

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private OkHttpClient client = new OkHttpClient();
    protected Callback callback;
    protected FailBehavior failBehavior = FailBehavior.CANCEL;

    public void download(Downloader.Task task) {
        Future<Task.Result> future = threadPool.submit(task);
        try {
            callback.onProgress(future.get());
            callback.onCompleted();
        } catch (Exception e) {
            callback.onFailed();
        }
    }

    public void downloadAll(List<Downloader.Task> tasks) {
        List<Future<Task.Result>> futures = new ArrayList<>(tasks.stream().map((t) -> threadPool.submit(t)).toList());

        while (!futures.isEmpty()) {
            Iterator<Future<Task.Result>> it = futures.iterator();
            while (it.hasNext()) {
                Future<Task.Result> future = it.next();
                if (!future.isDone())
                    continue;

                try {
                    callback.onProgress(future.get());
                    it.remove();
                } catch (Exception e) {
                    callback.onFailed();
                    e.printStackTrace();
                }
            }
        }
    }


    public Task taskFrom(URL url, Path path) {
        return new Task(url, path);
    }

    private Downloader() {}
}
