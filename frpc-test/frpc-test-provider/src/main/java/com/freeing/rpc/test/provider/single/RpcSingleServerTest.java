package com.freeing.rpc.test.provider.single;

import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * @author yanggy
 */
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer(){
        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880",
            "192.168.134.128:2181",
            "zookeeper",
            "random",
            "com.freeing.rpc.test",
            RpcConstants.REFLECT_TYPE_CGLIB, 10, 30);
        singleServer.startNettyServer();
    }
}
