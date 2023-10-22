package com.freeing.rpc.consumer.common.context;

import com.freeing.rpc.proxy.api.future.RPCFuture;

/**
 * 保存RPC上下文
 *
 * @author yanggy
 */
public class RpcContext {
    private RpcContext(){

    }

    /**
     * RpcContext实例
     */
    private static final RpcContext AGENT = new RpcContext();

    /**
     * 存放RPCFuture的InheritableThreadLocal
     */
    InheritableThreadLocal<RPCFuture> RPC_FUTURE_INHERITABLE_THREAD_LOCAL = new InheritableThreadLocal<>();


    public static RpcContext getContext(){
        return AGENT;
    }

    public void setRPCFuture(RPCFuture future) {
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.set(future);
    }

    public RPCFuture getRPCFuture() {
        return RPC_FUTURE_INHERITABLE_THREAD_LOCAL.get();
    }

    public void removeRPCFuture() {
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.remove();
    }
}
