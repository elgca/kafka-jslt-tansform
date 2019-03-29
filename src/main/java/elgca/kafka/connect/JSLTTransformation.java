package elgca.kafka.connect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;

public abstract class JSLTTransformation<R extends ConnectRecord<R>> implements Transformation<R> {
    private final static String JSTL_SCRIPT_URL = "script.url";
    private final static String JSTL_SCRIPT_TEXT = "script.text";
    private final static String BYTES_CHARSET = "charset";

    private final static ConfigDef CONFIG_DEF = new ConfigDef()
            .define(JSTL_SCRIPT_URL,
                    ConfigDef.Type.STRING,
                    null,
                    ConfigDef.Importance.HIGH,
                    "jstl script url"
            )

            .define(JSTL_SCRIPT_TEXT,
                    ConfigDef.Type.STRING,
                    null,
                    ConfigDef.Importance.HIGH,
                    "jstl script text"
            )

            .define(BYTES_CHARSET,
                    ConfigDef.Type.STRING,
                    "UTF-8",
                    ConfigDef.Importance.HIGH,
                    "byte[] to string charset"
            );


    private String charset = "UTF-8";
    private Expression jslt;
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    protected JSLTTransformation() {
    }

    @Override
    public R apply(R record) {
        if (record == null) {
            return null;
        }
        final Object _value = operatingValue(record);
        if (_value == null) {
            return record;
        }
        String value;
        if (_value instanceof String) {
            value = (String) _value;
        } else if (_value instanceof byte[]) {
            try {
                value = new String((byte[]) _value, charset);
            } catch (UnsupportedEncodingException e) {
                throw new DataException(e);
            }
        } else {
            throw new DataException("Only String/byte[] objects supported, found:" + _value.getClass().getName());
        }
        try {
            JsonNode node = mapper.readTree(value);
            JsonNode output = jslt.apply(node);
            return newRecord(record, null, mapper.writeValueAsString(output));
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public void close() {

    }

    protected abstract Schema operatingSchema(R record);

    protected abstract Object operatingValue(R record);

    protected abstract R newRecord(R record, Schema updatedSchema, Object updatedValue);

    @Override
    public void configure(Map<String, ?> map) {
        SimpleConfig config = new SimpleConfig(CONFIG_DEF, map);
        charset = config.getString(BYTES_CHARSET);
        String script = config.getString(JSTL_SCRIPT_TEXT);
        String script_url = config.getString(JSTL_SCRIPT_URL);
        if ((script != null && script_url != null)) {
            throw new ConfigException(JSTL_SCRIPT_URL, script_url, JSTL_SCRIPT_TEXT + " is already set");
        }
        if (script == null && script_url == null) {
            throw new ConfigException("must set " + JSTL_SCRIPT_URL + " or " + JSTL_SCRIPT_TEXT);
        }
        if (script == null) {
            try {
                script = IOUtils.toString(new URL(script_url), charset);
            } catch (IOException e) {
                throw new ConfigException(JSTL_SCRIPT_URL, script_url, e.getMessage());
            }
        }
        jslt = Parser.compileString(script);
    }

    public static class Value<R extends ConnectRecord<R>> extends JSLTTransformation<R> {
        public Value() {
        }

        protected Schema operatingSchema(R record) {
            return record.valueSchema();
        }

        protected Object operatingValue(R record) {
            return record.value();
        }

        protected R newRecord(R record, Schema updatedSchema, Object updatedValue) {
            return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(), updatedSchema, updatedValue, record.timestamp());
        }
    }

    public static class Key<R extends ConnectRecord<R>> extends JSLTTransformation<R> {
        public Key() {
        }

        protected Schema operatingSchema(R record) {
            return record.keySchema();
        }

        protected Object operatingValue(R record) {
            return record.key();
        }

        protected R newRecord(R record, Schema updatedSchema, Object updatedValue) {
            return record.newRecord(record.topic(), record.kafkaPartition(), updatedSchema, updatedValue, record.valueSchema(), record.value(), record.timestamp());
        }
    }
}
