package elgca.kafka.connect;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JSLTTransformationTest {
    private final JSLTTransformation<SinkRecord> xform = new JSLTTransformation.Key<>();
    private final URL script = getClass().getClassLoader().getResource("script.jslt");
    private final URL json = getClass().getClassLoader().getResource("test.json");
    private final URL except = getClass().getClassLoader().getResource("except.json");
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    @After
    public void teardown() {
        xform.close();
    }

    @Test
    public void stringRecord() throws IOException {
        xform.configure(Collections.singletonMap("script.url", script.toString()));
        getClass().getClassLoader().getResource("/test.json");
        String jsonText = IOUtils.toString(json, "UTF-8");
        String exceptText =
                mapper.writeValueAsString(
                        mapper.readTree(IOUtils.toString(except, "UTF-8"))
                );
        final SinkRecord record = new SinkRecord("test", 0, null, jsonText, null, null, 0);
        final SinkRecord transformedRecord = xform.apply(record);

        assertEquals(exceptText, transformedRecord.key());
    }
}