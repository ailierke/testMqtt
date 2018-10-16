package testmqtt;



import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import com.diwinet.util.DataReverse;

/**
 * Uses a Future based API to MQTT.
 */
class PublisherCallBackConnection implements Runnable{
	private Logger logger = Logger.getLogger(this.getClass().getName());
	String user =  "diwinet";
	String password = "dw_ywtc";
//	      String host = "cmd.wifi.d-water.net";
	      String host = "diwinet.ddns.net";
//	      String host = "60.205.163.8";
//	      String host = "net.d-water.cn";
//	      int port = Integer.parseInt("19301");
//	      String host = "123.56.187.173";
//	      String host = "192.168.0.106";
//	      String host = "diwinet.ddns.net";
	      int port = Integer.parseInt("19201");
//	      int port = Integer.parseInt("8081");
	Long sbtm = 7090161278008810028L;
	String sbtmStr= Long.toHexString(sbtm);
	final String destination = "/cmd/"+sbtmStr;
//	final String destination = "/cmd/5b74eb948d565216";
//	final String destination = "/dw/data";
	private int i = 0;
	public PublisherCallBackConnection(int i) {
		this.i = i;
	}
	public  void  run(){
		MQTT mqtt = new MQTT();
		try {
			mqtt.setHost(host, port);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		mqtt.setUserName(user);
		mqtt.setPassword(password);
		mqtt.setCleanSession(true);
		mqtt.setClientId("j_getdata"+i);
		//失败重连接设置说明
		mqtt.setConnectAttemptsMax(10L);//客户端首次连接到服务器时，连接的最大重试次数，超出该次数客户端将返回错误。-1意为无重试上限，默认为-1
		mqtt.setReconnectAttemptsMax(3L);//客户端已经连接到服务器，但因某种原因连接断开时的最大重试次数，超出该次数客户端将返回错误。-1意为无重试上限，默认为-1
		mqtt.setReconnectDelay(10L);//首次重连接间隔毫秒数，默认为10ms
		mqtt.setReconnectDelayMax(30000L);//重连接间隔毫秒数，默认为30000ms
		//        mqtt.setReconnectBackOffMultiplier(2);//设置重连接指数回归。设置为1则停用指数回归，默认为2
		mqtt.setReceiveBufferSize(65536);//设置socket接收缓冲区大小，默认为65536（64k）
		mqtt.setSendBufferSize(65536);//设置socket发送缓冲区大小，默认为65536（64k）
		mqtt.setTrafficClass(8);//设置发送数据包头的流量类型或服务类型字段，默认为8，意为吞吐量最大化传输
//		mqtt.setWillRetain(willRetain);
		//带宽限制设置说明
		//        mqtt.setMaxReadRate(0);//设置连接的最大接收速率，单位为bytes/s。默认为0，即无限制
		//        mqtt.setMaxWriteRate(0);//设置连接的最大发送速率，单位为bytes/s。默认为0，即无限制
		CallbackConnection connection = mqtt.callbackConnection();
			connection.connect(new Callback<Void>() {
				public void onFailure(Throwable value) {
					System.out.println("connected valid");
				}
				// Once we connect..
				public void onSuccess(Void v) {
					System.out.println("connect success"+i+"个客户端已经连接成功");
				}
			});
//			String sendMsg = "{\"sbtm\":\""+sbtmStr+"\",\"k\":1,\"v\":\"06404040404040\"}";
//			String sendMsg = "{\"sbtm\":\""+sbtmStr+"\",\"k\":2,\"v\":\"1\"}";//关机
//			String sendMsg = "{\"sbtm\":\""+sbtmStr+"\",\"k\":3,\"v\":\"1\"}";//制水
			String sendMsg = "{\"sbtm\":\""+sbtmStr+"\",\"k\":2,\"v\":\"1\"}";//开机
//			String sendMsg = null;
//				sendMsg = "{\"sbtm\":\""+sbtmStr+"\",\"k\":12,\"v\":\""+"upload.wifi.d-water.net:19101"+"\"}";
//				sendMsg = "{\"sbtm\":\""+sbtmStr+"\",\"k\":12,\"v\":\""+"net.d-water.cn:19301"+"\"}";
		//开机
//			while(count<100){
//				count++;
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				//	        	sendMsg = "{\"sbtm\":\""+sbtmStr+"\",\"k\":1,\"v\":\"06404040404040\"}";
////				sendMsg = "{\"sbtm\":\""+sbtmStr+"\",\"k\":7,\"v\":\"1\"}";
////				sendMsg = "{\"bssj\":1470365716905,\"csls\":0,\"cssl\":354,\"cssz\":1,\"sbtm\":\"7090160778008830114\",\"tbsj\":1470365716906,\"time\":1470365716906,\"wllx\":0,\"ysls\":0,\"yssl\":0,\"yssz\":60} ";
//				sendMsg = "这是线程【"+i+"】发送的第【"+count+"】条数据";
//			connection.publish(destination, sendMsg.getBytes(), QoS.AT_MOST_ONCE, false, new MqttPushCallBack(sendMsg));
				connection.publish(destination, sendMsg.getBytes(), QoS.AT_MOST_ONCE, false, new MqttPushCallBack(sendMsg));
//			}
	}
}
