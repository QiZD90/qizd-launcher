package ml.qizd.qizdlauncher.apis;

import com.google.gson.Gson;
import ml.qizd.qizdlauncher.models.ElyByResponse;
import ml.qizd.qizdlauncher.users.ElyByUserProfile;
import okhttp3.*;

import java.io.IOException;

public class ElyByApi {
    private static final String API_URL = "https://authserver.ely.by";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static ElyByUserProfile auth(String username, String password) throws Exception {
        FormBody form = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("clientToken", "qizd-launcher")
                .add("requestUser", "true")
                .build();

        Request request = new Request.Builder()
                .url(API_URL + "/auth/authenticate")
                .post(form)
                .build();

        try (Response response = client.newCall(request).execute(); ResponseBody body = response.body()) {
            if (!response.isSuccessful() || body == null)
                throw new IOException("Failed to contact ely.by");

            ElyByResponse e = gson.fromJson(body.charStream(), ElyByResponse.class);
            if (e.error != null)
                throw new Exception(e.error + "\n" + e.errorMessage);

            return new ElyByUserProfile(e.user.username, e.accessToken, e.user.id);
        }
    }

    public static ElyByUserProfile refresh(ElyByUserProfile profile) throws Exception {
        FormBody form = new FormBody.Builder()
                .add("accessToken", profile.getAccessToken())
                .add("clientToken", "qizd-launcher")
                .add("requestUser", "true")
                .build();

        Request request = new Request.Builder()
                .url(API_URL + "/auth/refresh")
                .post(form)
                .build();

        try (Response response = client.newCall(request).execute(); ResponseBody body = response.body()) {
            if (!response.isSuccessful() || body == null)
                throw new IOException("Failed to contact ely.by");

            ElyByResponse e = gson.fromJson(body.charStream(), ElyByResponse.class);
            if (e.error != null)
                throw new Exception(e.error + "\n" + e.errorMessage);

            return new ElyByUserProfile(e.user.username, e.accessToken, e.user.id);
        }
    }
}
