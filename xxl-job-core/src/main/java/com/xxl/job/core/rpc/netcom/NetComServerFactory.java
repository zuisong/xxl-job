package com.xxl.job.core.rpc.netcom;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.rpc.codec.RpcRequest;
import com.xxl.job.core.rpc.codec.RpcResponse;
import com.xxl.job.core.rpc.netcom.jetty.server.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * netcom init
 *
 * @author xuxueli 2015-10-31 22:54:27
 */
public class NetComServerFactory {
    private static final Logger logger = LoggerFactory.getLogger(NetComServerFactory.class);

    // ---------------------- server start ----------------------
    JettyServer server = new JettyServer();

    public void start(int port, String ip, String appName) throws Exception {
        server.start(port, ip, appName);
    }

    // ---------------------- server destroy ----------------------
    public void destroy() {
        server.destroy();
    }

    // ---------------------- server instance ----------------------
    /**
     * init local rpc service map
     */
    private static Map<String, Object> serviceMap = new HashMap<String, Object>();
    private static String accessToken;

    public static void putService(Class<?> iface, Object serviceBean) {
        serviceMap.put(iface.getName(), serviceBean);
    }

    public static void setAccessToken(String accessToken) {
        NetComServerFactory.accessToken = accessToken;
    }
    // todo 根据rpc的请求内容作出响应的动作
    public static RpcResponse invokeService(RpcRequest request, Object serviceBean) {
        if (serviceBean == null) {
            /**
             * todo 取出之前放入的service
             * 放入的地方在下面
             * @see XxlJobExecutor#initExecutorServer(int, java.lang.String, java.lang.String, java.lang.String)
             * @see com.xxl.job.admin.core.schedule.XxlJobDynamicScheduler#init()
             */
            serviceBean = serviceMap.get(request.getClassName());
        }
        if (serviceBean == null) {
            // TODO
        }

        RpcResponse response = new RpcResponse();
        // todo 180 秒过期，不执行任务
        if (System.currentTimeMillis() - request.getCreateMillisTime() > 180000) {
            response.setResult(new ReturnT<String>(ReturnT.FAIL_CODE, "The timestamp difference between admin and executor exceeds the limit."));
            return response;
        }
        // todo token校验
        if (accessToken != null && accessToken.trim().length() > 0 && !accessToken.trim().equals(request.getAccessToken())) {
            response.setResult(new ReturnT<String>(ReturnT.FAIL_CODE, "The access token[" + request.getAccessToken() + "] is wrong."));
            return response;
        }

        try {
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] parameters = request.getParameters();

            FastClass serviceFastClass = FastClass.create(serviceClass);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
            // todo 反射执行方法
            Object result = serviceFastMethod.invoke(serviceBean, parameters);

            response.setResult(result);
        } catch (Throwable t) {
            t.printStackTrace();
            response.setError(t.getMessage());
        }

        return response;
    }

}
