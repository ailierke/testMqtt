package testmqtt;


import java.util.Arrays;
import java.util.logging.Logger;

import org.fusesource.hawtbuf.*;
import org.fusesource.mqtt.client.*;

/**
 * Uses an callback based interface to MQTT.  Callback based interfaces
 * are harder to use but are slightly more efficient.
 */
class Listener {
	private static  Logger log = Logger.getLogger(Listener.class.getName());
    public static void main(String []args) throws Exception {
    	System.out.println(Long.toHexString(7090161278008866025l));
        String user = "diwinet";
        String password = "dw_ywtc";
//        String host = "192.168.0.106";
//        int port =1883;
//          String host = "cmd.wifi.d-water.net";
          String host = "192.168.0.52";
//	      String host = "60.205.163.8";
	      int port = Integer.parseInt("19201");
	      Long sbtm = 7090161278008810028L;
	  		String sbtmStr= Long.toHexString(sbtm);
	      final String destination = "/cmd/"+sbtmStr;
//	      final String destination = "/dw/data";
//        final String destination = arg(args, 0, "/cmd/5b74eb948d565216");
//        final String destination = arg(args, 0, "/cmd/"+Long.toHexString(6590151278008802008l));
//        final String destination = arg(args, 0, "/cmd/"+Long.toHexString(6590151278008802002l));
       


        MQTT mqtt = new MQTT();
        mqtt.setHost(host, port);
        mqtt.setUserName(user);
        mqtt.setPassword(password);
        mqtt.setCleanSession(true);
        mqtt.setClientId("test322dwqd2");
        //失败重连接设置说明
        mqtt.setConnectAttemptsMax(1L);//客户端首次连接到服务器时，连接的最大重试次数，超出该次数客户端将返回错误。-1意为无重试上限，默认为-1
        mqtt.setReconnectAttemptsMax(3L);//客户端已经连接到服务器，但因某种原因连接断开时的最大重试次数，超出该次数客户端将返回错误。-1意为无重试上限，默认为-1
        mqtt.setReconnectDelay(10L);//首次重连接间隔毫秒数，默认为10ms
        mqtt.setReconnectDelayMax(30000L);//重连接间隔毫秒数，默认为30000ms
//        mqtt.setReconnectBackOffMultiplier(2);//设置重连接指数回归。设置为1则停用指数回归，默认为2
        mqtt.setReceiveBufferSize(65536);//设置socket接收缓冲区大小，默认为65536（64k）
        mqtt.setSendBufferSize(65536);//设置socket发送缓冲区大小，默认为65536（64k）
        mqtt.setTrafficClass(8);//设置发送数据包头的流量类型或服务类型字段，默认为8，意为吞吐量最大化传输
        
      //带宽限制设置说明
//        mqtt.setMaxReadRate(0);//设置连接的最大接收速率，单位为bytes/s。默认为0，即无限制
//        mqtt.setMaxWriteRate(0);//设置连接的最大发送速率，单位为bytes/s。默认为0，即无限制

        final CallbackConnection connection = mqtt.callbackConnection();
        connection.listener(new org.fusesource.mqtt.client.Listener() {
            public void onConnected() {
            	System.out.println("connected！！");
            }
            public void onDisconnected() {
            	System.out.println("connect valid！！");
            }
            public void onFailure(Throwable value) {
                log.info("error:"+value.getMessage());
            }
            public void onPublish(UTF8Buffer topic, Buffer msg, Runnable ack) {
                String body = msg.utf8().toString();
                System.out.println("receive sucess 【"+body+"】");
            }
        });
        connection.connect(new Callback<Void>() {
            public void onSuccess(Void value) {
                Topic[] topics = {new Topic(destination, QoS.AT_MOST_ONCE)};
                connection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {
                    	System.out.println(Arrays.toString(qoses));
                    }
                    public void onFailure(Throwable value) {
                    	 log.info("error:"+value.getMessage());
                    }
                });
            }
            public void onFailure(Throwable value) {
            	log.info("error:"+value.getMessage());
            }
        });

        // Wait forever..
        synchronized (Listener.class) {
            while(true)
                Listener.class.wait();
        }
    }
}
