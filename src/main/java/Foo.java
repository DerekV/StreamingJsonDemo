import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class Foo {

    // an example from the github api docs
    public static final String ENDPOINT = "https://api.github.com/search/issues?q=windows+label:bug+language:python+state:open";
    public static final SerializedString ITEMS = new SerializedString("items");

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
                JsonFactory jsonFactory = new JsonFactory(); // or, for data binding, org.codehaus.jackson.mapper.MappingJsonFactory
                JsonParser jp = jsonFactory.createParser(stream);

                // find and consume object start

                while (jp.nextToken() != JsonToken.START_OBJECT) {
                    // do nothing
                }

                // find the items
                while (!jp.nextFieldName(ITEMS)) {
                }
                // now get stuff

                while (jp.nextToken() != null) {
                    JsonToken token = jp.getCurrentToken();
                    if(token == JsonToken.FIELD_NAME) {
                        String fieldName = jp.getValueAsString();
                        if("title".equals(fieldName)) {
                            System.out.println("title = " + jp.nextTextValue());
                        } else if ("number".equals(fieldName)) {
                            System.out.println("number = " + jp.nextIntValue(-1));
                        }
                    }
                }




            }
        });

    }

}
