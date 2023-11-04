package com.freeing.rpc.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.freeing.rpc.common.exception.SerializerException;
import com.freeing.rpc.serialization.api.Serialization;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Kryo 序列化
 *
 * @author yanggy
 */
@SPIClass
public class KryoSerialization implements Serialization {
    private static final Logger logger = LoggerFactory.getLogger(KryoSerialization.class);


    @Override
    public <T> byte[] serialize(T object) {
        logger.info("execute kryo serialize ...");
        if (Objects.isNull(object)) {
            throw new SerializerException("serialize object is null");
        }

        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(object.getClass(), new JavaSerializer());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Output output = new Output(out);
        kryo.writeClassAndObject(output, object);
        output.flush();
        output.close();
        byte[] bytes = out.toByteArray();

        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
        return bytes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        logger.info("execute kryo deserialize ...");
        if (Objects.isNull(bytes)) {
            throw new SerializerException("deserialize data can not be null");
        }
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(clazz, new JavaSerializer());
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Input input = new Input(bais);
        return (T) kryo.readClassAndObject(input);
    }
}
