package com.freeing.rpc.serialization.jdk;


import com.freeing.rpc.common.exception.SerializerException;
import com.freeing.rpc.serialization.api.Serialization;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author yanggy
 */
@SPIClass
public class JdkSerialization implements Serialization {
    private static final Logger logger = LoggerFactory.getLogger(JdkSerialization.class);

    @Override
    public <T> byte[] serialize(T object) {
        logger.info("execute jdk serialize ...");
        if (object == null) {
            throw new SerializerException("serialize object can not be null");
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeObject(object);
            return os.toByteArray();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        logger.info("execute jdk deserialize ...");
        if (bytes == null) {
            throw new SerializerException("deserialize byte array can not be null");
        }
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(is);
            return (T) in.readObject();
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }
}
