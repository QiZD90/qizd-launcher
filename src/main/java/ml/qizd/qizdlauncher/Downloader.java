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
    public class Task implements Supplier<Task.Result> {
        @Override
        public Result get() {
            try (Response response = client.newCall(request).execute(); ResponseBody body = response.body()) {
                if (!response.isSuccessful() || body == null) {
                    if (failBehavior.equals(FailBehavior.CANCEL))
                        throw new Exception("Failed to download " + filePath.toString());

                    return null;
                }

                filePath.getParent().toFile().mkdirs();
                try (OutputStream stream = Files.newOutputStream(filePath)){
                    body.byteStream().transferTo(stream);
                }

                return new Result(filePath.toString());
            } catch (Exception e) {
                if (failBehavior.equals(FailBehavior.CANCEL))
                    throw new RuntimeException(e);
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
        CANCEL,
        IGNORE
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
            downloader.threadPool = Executors.newFixedThreadPool(threads, (r) -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });

            return downloader;
        }

        private Builder() {}
    }

    protected ExecutorService threadPool;
    private final OkHttpClient client = new OkHttpClient();
    protected Callback callback;
    protected FailBehavior failBehavior = FailBehavior.CANCEL;

    public void download(Downloader.Task task) {
        Future<Task.Result> future = CompletableFuture.supplyAsync(task);
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
        } catch (Exception e) {
            callback.onFailed();
        }
    }


    public Task taskFrom(URL url, Path path) {
        return new Task(url, path);
    }

    private Downloader() {}
}
