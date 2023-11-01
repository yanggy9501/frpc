package com.freeing.rpc.serialization.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.freeing.rpc.common.exception.SerializerException;
import com.freeing.rpc.serialization.api.Serialization;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * JSON 系列化与反序列化
 *
 * @author yanggy
 */
@SPIClass
public class JsonSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(JsonSerialization.class);

    /**
     * jackson 工具类
     */
    private static ObjectMapper objMapper = new ObjectMapper();

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objMapper.setDateFormat(dateFormat);
        objMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        objMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        objMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        objMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    }

    @Override
    public <T> byte[] serialize(T object) {
        logger.info("execute json serialize ...");
        if (object == null) {
            throw new SerializerException("serialize object is null");
        }
        byte[] bytes;
        try {
            bytes = objMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new SerializerException(e.getMessage(), e);
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        logger.info("execute json deserialize...");
        if (clazz == null) {
            throw new SerializerException("serialize object is null");
        }
        T obj;
        try {
            obj = objMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
        return obj;
    }
}
