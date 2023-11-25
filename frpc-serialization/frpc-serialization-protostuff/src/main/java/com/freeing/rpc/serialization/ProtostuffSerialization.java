package com.freeing.rpc.serialization;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.freeing.rpc.common.exception.SerializerException;
import com.freeing.rpc.serialization.api.Serialization;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rotostuff 序列化
 *
 * @author yanggy
 */
@SPIClass
public class ProtostuffSerialization implements Serialization {
    private static final Logger logger = LoggerFactory.getLogger(ProtostuffSerialization.class);

    private final Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private Objenesis objenesis = new ObjenesisStd(true);

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(clazz);
        if (Objects.isNull(schema)) {
            synchronized (this) {
                schema = (Schema<T>) cachedSchema.get(clazz);
                if (Objects.isNull(schema)) {
                    cachedSchema.put(clazz, RuntimeSchema.createFrom(clazz));
                    schema = (Schema<T>) cachedSchema.get(clazz);
                }
            }
        }
        return schema;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serialize(T object) {
        logger.info("execute protostuff serialize...");
        if (Objects.isNull(object)) {
            throw new SerializerException("serialize object can not be null");
        }
        Class<T> clazz = (Class<T>) object.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            return ProtostuffIOUtil.toByteArray(object, schema, buffer);
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        logger.info("execute protostuff deserialize...");
        if (Objects.isNull(bytes)) {
            throw new SerializerException("deserialize data can not be null");
        }
        try {
            T message = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(bytes, message, schema);
            return message;
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }
}
