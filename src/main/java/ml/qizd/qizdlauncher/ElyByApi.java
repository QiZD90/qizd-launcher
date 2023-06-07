package ml.qizd.qizdlauncher;

import com.google.gson.Gson;
import okhttp3.*;

public class ElyByApi {
    private static final String API_URL = "https://authserver.ely.by";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static String getAuthToken(String username, String password) throws Exception {
        FormBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("clientToken", "asdfsdf")
                .add("requestUser", "true")
                .build();

        Request request = new Request.Builder()
                .url(API_URL + "/auth/authenticate")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
