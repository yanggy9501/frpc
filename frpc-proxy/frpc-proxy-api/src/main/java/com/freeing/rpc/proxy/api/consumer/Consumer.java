package com.freeing.rpc.proxy.api.consumer;

import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.proxy.api.future.RPCFuture;

/**
 * 服务消费者
 *
 * @author yanggy
 */
public interface Consumer {

    RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol) throws Exception;
}
