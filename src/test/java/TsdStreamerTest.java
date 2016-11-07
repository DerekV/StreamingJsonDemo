import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TsdStreamerTest {

    public static final int PERIOD = 200;  // SET ME HIGHER TO WATCH IT GO SLOWER, LOWER IF YOU WANT TO FINISH ONE DAY
    public static final int BIGTEST_SIZE = 100000;  // HOW MANY SAMPLES SHOULD WE GENERATE FOR BIGTEST

    @Test(timeout = 4000)
    public void smallTest() throws IOException, InterruptedException {
        MockWebServer server = new MockWebServer();

        Buffer bodyGenerator;
        bodyGenerator = new Buffer();


        generateSmallResponse(bodyGenerator);

        server.enqueue(new MockResponse().setBody(bodyGenerator));
        server.start();

        HttpUrl baseUrl = server.url("/changeme/");

        TsdStreamer tStrm = new TsdStreamer(baseUrl);
        tStrm.executeQuery();  // execute query will be async ... sortof hacking it

        server.takeRequest();

        int finalCount = tStrm.blockForCount();  // wait for receiving end to complete parsing
        assertThat(finalCount, is(3));

        server.shutdown();
    }

    @Test
    public void bigTest() throws IOException, InterruptedException {
        MockWebServer server = new MockWebServer();

        final Buffer bodyGenerator = new Buffer();


        generateBigResponse(bodyGenerator, BIGTEST_SIZE);

        // I tried to also generate it slowly, to cause streming end to end, but it seems that this didn't play well
        // with mockwebserver, which seems to send whatever is in the buffer now as the whole request whenever the
        // request actually occures... should inspect the mockwebserver code deeper to see what is going on
        // the throttlebody gives us what we need to know however ...
//        Thread generatorThread = new Thread(() -> {
//            try {
//                generateBigResponse(bodyGenerator,10000000,30);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });

        final MockResponse mockResponse = new MockResponse();
        mockResponse.setChunkedBody(bodyGenerator, 4096).throttleBody(3000, PERIOD, TimeUnit.MILLISECONDS);
        server.enqueue(mockResponse);
        server.start();

        //generatorThread.start();

        HttpUrl baseUrl = server.url("/changeme/");

        TsdStreamer tStrm = new TsdStreamer(baseUrl);
        tStrm.executeQuery();  // execute query will be async ... sortof hacking it

        server.takeRequest();

        int finalCount = tStrm.blockForCount();  // wait for receiving end to complete parsing


        //generatorThread.join();
        assertThat(finalCount, is(BIGTEST_SIZE));


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

    private void generateBigResponse(Buffer pipe, long rows) throws IOException, InterruptedException {
        long psuedoTime = 1365966001;
        Random random = new Random(1234);

        pipe.writeUtf8("[\n" +
                "    {\n" +
                "        \"metric\": \"tsd.hbase.puts\",\n" +
                "        \"tags\": {},\n" +
                "        \"aggregatedTags\": [\n" +
                "            \"host\"\n" +
                "        ],\n" +
                "        \"dps\": [\n");
        pipe.flush();
        for (int i = 0; i < rows - 1; i++) {
            pipe.writeUtf8("            [\n                ");
            pipe.writeUtf8(Long.toString(psuedoTime));
            pipe.writeUtf8(",\n                ");
            pipe.writeUtf8(Long.toString(random.nextLong()));
            pipe.writeUtf8("            ],\n");
            pipe.flush();
            psuedoTime += random.nextInt(120);
            //Thread.sleep(delayMs);
        }
        if (rows > 0) {
            pipe.writeUtf8("            [\n                ");
            pipe.writeUtf8(Long.toString(psuedoTime));
            pipe.writeUtf8(",\n                ");
            pipe.writeUtf8(Long.toString(random.nextLong()));
            pipe.writeUtf8("            ]\n");  // no comma
        }


        pipe.writeUtf8(
                "        ]\n" +
                        "    }\n" +
                        "]");
        pipe.flush();
        pipe.close();
    }

}
