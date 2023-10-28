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
            "127.0.0.1:2181",
            "zookeeper",
            "com.freeing.rpc.test",
            RpcConstants.REFLECT_TYPE_CGLIB);
        singleServer.startNettyServer();
    }
}
