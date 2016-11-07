import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TsdStreamerTest {

    @Test(timeout=4000)
    public void smallTest() throws IOException, InterruptedException {
        MockWebServer server = new MockWebServer();

        Buffer bodyGenerator;
        bodyGenerator = new Buffer();


        generateSmallResponse(bodyGenerator);

        server.enqueue(new MockResponse().setBody(bodyGenerator));
                //.throttleBody(20,10, TimeUnit.MILLISECONDS));
        server.start();

        HttpUrl baseUrl = server.url("/changeme/");

        TsdStreamer tStrm = new TsdStreamer(baseUrl);
        tStrm.executeQuery();  // execute query will be async ... sortof hacking it

        server.takeRequest();

        int finalCount = tStrm.blockForCount();  // wait for receiving end to complete parsing
        assertThat(finalCount, is(3));

        server.shutdown();
    }

    private void generateSmallResponse(Buffer pipe) throws IOException {
        pipe.writeUtf8("[\n" +
                "    {\n" +
                "        \"metric\": \"tsd.hbase.puts\",\n" +
                "        \"tags\": {},\n" +
                "        \"aggregatedTags\": [\n" +
                "            \"host\"\n" +
                "        ],\n" +
                "        \"dps\": [\n" +
                "            [\n" +
                "                1365966001,\n" +
                "                25595461080\n" +
                "            ],\n" +
                "            [\n" +
                "                1365966061,\n" +
                "                25595542522\n" +
                "            ],\n" +
                "            [\n" +
                "                1365974221,\n" +
                "                25722266376\n" +
                "            ]\n" +
                "        ]\n" +
                "    }\n" +
                "]");
        pipe.flush();
        pipe.close();
    }

}
