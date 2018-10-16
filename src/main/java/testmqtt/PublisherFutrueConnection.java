package testmqtt;



import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.util.LinkedList;

/**
 * Uses a Future based API to MQTT.
 */
class PublisherFutrueConnection {

    public static void main(String []args) throws Exception {

        String user = env("APOLLO_USER", "admin");
        String password = env("APOLLO_PASSWORD", "password");
        String host = env("APOLLO_HOST", "net.d-water.cn");
        int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
        final String destination = arg(args, 0, "diwinet/test");


        String body ="hello world";
        Buffer msg =null;

        MQTT mqtt = new MQTT();
        mqtt.setHost(host, port);
        mqtt.setUserName(user);
        mqtt.setPassword(password);
        mqtt.setCleanSession(true);
        //失败重连接设置说明
        mqtt.setConnectAttemptsMax(10L);//客户端首次连接到服务器时，连接的最大重试次数，超出该次数客户端将返回错误。-1意为无重试上限，默认为-1
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
        FutureConnection connection = mqtt.futureConnection();
        connection.connect().await();
        
        UTF8Buffer topic = new UTF8Buffer(destination);

            // Send the publish without waiting for it to complete. This allows us
        Integer count = 0;
        String sendMsg = "";
        while(count<101){
        	Thread.sleep(20);
        	sendMsg = body+count;
        	msg= new AsciiBuffer(sendMsg);
        	connection.publish(topic, msg, QoS.EXACTLY_ONCE, false);
        	System.out.println("已经发送的数量"+count);
        	System.out.println("发送的数据为："+sendMsg);
        	count++;
        }

            // Eventually we start waiting for old publish futures to complete
            // so that we don't create a large in memory buffer of outgoing message.s

        connection.disconnect().await();
    }

    private static String env(String key, String defaultValue) {
        String rc = System.getenv(key);
        if( rc== null )
            return defaultValue;
        return rc;
    }

    private static String arg(String []args, int index, String defaultValue) {
        if( index < args.length )
            return args[index];
        else
            return defaultValue;
    }
}
