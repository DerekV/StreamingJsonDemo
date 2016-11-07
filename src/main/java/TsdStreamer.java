import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TsdStreamer {
    private final HttpUrl baseUrl;
    private final CountDownLatch latch = new CountDownLatch(1);
    private int count = 0;

    public TsdStreamer(HttpUrl baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void executeQuery() {
        if(latch.getCount()==0) {
            throw new IllegalStateException("TsdStreamer should only be executed once");
        }
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        final OkHttpClient okHttpClient = builder.build();

        Request request = new Request.Builder()
                .url(queryEndpoint())
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                // oops
                System.out.println("Fail");
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("in onResponse");
                final InputStream stream = response.body().byteStream();
                JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));

                // TODO ... a gotchya I've noticed, is we can block forever if we don't get things right
                // need to look why that might be, because I'm worried it could happen with malformed JSON

                reader.setLenient(true);
                reader.beginArray();
                while(reader.hasNext()) {
                    reader.beginObject();
                    while(reader.hasNext()) {
                        switch (reader.nextName()) {
                            case "metric":
                                System.out.println(reader.nextString());
                                break;
                            case "dps":
                                readTimeSeries(reader); break;
                            default:
                                reader.skipValue();
                        }
                    }
                    reader.endObject();
                }
                reader.endArray();
                reader.close();
                latch.countDown();
            }
        });
    }


    public int blockForCount() throws InterruptedException {
        // the whole latch and execute-once is kindof a hack... but i'm just trying to prove it is actually streaming
        // and this is helpful for testing
        latch.await();
        return count;
    }

    private HttpUrl queryEndpoint() {
        // TODO construct actual query URL
        return baseUrl;
    }

    private void readTimeSeries(JsonReader reader) throws IOException {
        reader.beginArray();
        while(reader.hasNext()) {
            reader.beginArray();
            long ts = reader.nextLong();
            long val = reader.nextLong();
            System.out.println(Long.toString(ts) + ":" + Long.toString(val));
            reader.endArray();
            count++;
        }
        reader.endArray();
    }
}
