
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class GithubIssuesExample {

    // an example from the github api docs
    private static final String ENDPOINT = "https://api.github.com/search/issues?q=windows+label:bug+language:python+state:open";

    public static void main(String args[]) {

        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        final OkHttpClient okHttpClient = builder.build();

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                // oops
                System.out.println("Fail");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final InputStream stream = response.body().byteStream();
                JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
                Gson gson = new GsonBuilder().create();

                boolean foundItems = false;
                reader.setLenient(true);
                reader.beginObject();
                while(reader.hasNext()) {
                    String fieldName = reader.nextName();
                    if("items".equals(fieldName)) {
                        foundItems = true;
                        readItems(reader);
                    } else {
                        reader.skipValue();
                    }

                }
                reader.endObject();

                if(!foundItems) {
                    System.out.println("I did not find the \"items\" field");
                }

            }
        });

    }

    private static void readItems(JsonReader reader) throws IOException {
        Gson gson = new GsonBuilder().create();
        int count = 0;
        reader.beginArray();
        while(reader.hasNext()) {
            GithubIssue issue = gson.fromJson(reader,GithubIssue.class);
            System.out.println(issue.toString());
            count++;
        }
        reader.endArray();

        System.out.println("counted " + count + "issues");
    }


}
