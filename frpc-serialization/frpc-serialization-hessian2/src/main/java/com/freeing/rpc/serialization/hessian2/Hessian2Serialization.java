package com.freeing.rpc.serialization.hessian2;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
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
 * @author yanggy
 */
@SPIClass
public class Hessian2Serialization implements Serialization {
    private static final Logger logger = LoggerFactory.getLogger(Hessian2Serialization.class);

    @Override
    public <T> byte[] serialize(T object) {
        logger.info("execute hessian2 Serialize ...");
        if (Objects.isNull(object)) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(outputStream);
        byte[] result;
        try {
            hessian2Output.startMessage();
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            hessian2Output.completeMessage();
            result = outputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            try {
                hessian2Output.close();
                outputStream.close();
            } catch (IOException e) {
                throw new SerializerException(e.getMessage(), e);
            }
        }
        return result;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        logger.info("execute hessian2 deserialize ...");
        if (Objects.isNull(bytes)) {
            throw new SerializerException("deserialize data is null");
        }

        T result;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        Hessian2Input hessian2Input = new Hessian2Input(inputStream);
        try {
            hessian2Input.startMessage();
            result = (T)hessian2Input.readObject();
            hessian2Input.completeMessage();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            try {
                hessian2Input.close();
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
