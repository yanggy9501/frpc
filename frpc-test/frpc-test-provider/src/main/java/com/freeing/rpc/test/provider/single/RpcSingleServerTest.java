package com.freeing.rpc.test.provider.single;

import com.freeing.rpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * @author yanggy
 */
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer(){
        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880", "com.freeing.rpc.test");
        singleServer.startNettyServer();
    }
}