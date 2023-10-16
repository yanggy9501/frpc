package com.freeing.rpc.serialization.jdk;


import com.freeing.rpc.common.exception.SerializerException;
import com.freeing.rpc.serialization.api.Serialization;

import java.io.*;

/**
 * @author yanggy
 */
public class JdkSerialization implements Serialization {
    @Override
    public <T> byte[] serialize(T object) {
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
