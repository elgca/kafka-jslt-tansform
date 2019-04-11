package elgca.kafka.connect;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

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
        Map<?, ?> exceptText =
                mapper.convertValue(
                        mapper.readTree(IOUtils.toString(except, "UTF-8")),
                        Map.class
                );
        final SinkRecord record = new SinkRecord("test", 0, null, jsonText, null, null, 0);
        final SinkRecord transformedRecord = xform.apply(record);

        assertEquals(exceptText, transformedRecord.key());
    }

    @Test
    public void bytesRecordText() throws IOException {
        xform.configure(Collections.singletonMap("script.text", IOUtils.toString(script)));
        getClass().getClassLoader().getResource("/test.json");
        byte[] jsonText = IOUtils.toString(json, "UTF-8").getBytes();
        Map<?, ?> exceptText =
                mapper.convertValue(
                        mapper.readTree(IOUtils.toString(except, "UTF-8")),
                        Map.class
                );
        final SinkRecord record = new SinkRecord("test", 0, null, jsonText, null, null, 0);
        final SinkRecord transformedRecord = xform.apply(record);

        assertEquals(exceptText, transformedRecord.key());
    }

    @Test
    public void structRecord() throws IOException {
        final Schema keySchema = SchemaBuilder.struct()
                .field("schema", Schema.STRING_SCHEMA)
                .field("id", Schema.STRING_SCHEMA)
                .field("published", Schema.STRING_SCHEMA)
                .field("type", Schema.STRING_SCHEMA)
                .field("environmentId", Schema.STRING_SCHEMA)
                .field("url", Schema.STRING_SCHEMA)
                .build();
        final Struct key = new Struct(keySchema)
                .put("schema", "http://schemas.schibsted.io/thing/pulse-simple.json#1.json")
                .put("id", "w23q7ca1-8729-24923-922b-1c0517ddffjf1")
                .put("published", "2017-05-04T09:13:29+02:00")
                .put("type", "View")
                .put("environmentId", "urn:schibsted.com:environment:uuid")
                .put("url", "http://www.aftenposten.no/");
        xform.configure(Collections.singletonMap("script.url", script.toString()));
        getClass().getClassLoader().getResource("/test.json");
        Map<?, ?> exceptText =
                mapper.convertValue(
                        mapper.readTree(IOUtils.toString(except, "UTF-8")),
                        Map.class
                );
        final SinkRecord record = new SinkRecord("test", 0, keySchema, key, null, null, 0);
        final SinkRecord transformedRecord = xform.apply(record);

        assertEquals(exceptText, transformedRecord.key());
    }
}