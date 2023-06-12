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
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Downloader {
    public class Task implements Callable<Task.Result>, Supplier<Task.Result> {
        @Override
        public Result get() {
            try {
                return this.call();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public static class Result {
            public String filePath;
            public Result(String filePath) {
                this.filePath = filePath;
            }
        }

        Request request;
        Path filePath;

        public Result call() throws IOException {
            try (Response response = client.newCall(request).execute(); ResponseBody body = response.body()) {
                if (!response.isSuccessful() || body == null)
                    throw new IOException("Failed to download " + filePath.toString());

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
        int threads = 1;
        Callback callback;
        FailBehavior failBehavior = FailBehavior.CANCEL;

        public Builder failBehavior(FailBehavior failBehavior) {
            this.failBehavior = failBehavior;
            return this;
        }

        public Builder threads(int threads) {
            this.threads = threads;
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
            downloader.threadPool = Executors.newFixedThreadPool(threads);

            return downloader;
        }

        private Builder() {}
    }

    protected ExecutorService threadPool;
    private final OkHttpClient client = new OkHttpClient();
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
        CompletableFuture<?>[] futures = new CompletableFuture[tasks.size()];
        tasks
                .stream()
                .map((t) -> CompletableFuture
                        .supplyAsync(t, threadPool)
                        .thenAcceptAsync((x) -> callback.onProgress(x))
                )
                .toList()
                .toArray(futures);


        try {
            CompletableFuture.allOf(futures).join();
            callback.onCompleted();
        } catch (CompletionException e) {
            callback.onFailed();
        }
    }


    public Task taskFrom(URL url, Path path) {
        return new Task(url, path);
    }

    private Downloader() {}
}
