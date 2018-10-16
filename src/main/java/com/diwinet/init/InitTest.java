package com.diwinet.init;


import java.util.Arrays;
import java.util.logging.Logger;

import org.fusesource.mqtt.client.*;

import com.diwinet.service.DataHandlerListener;
import com.diwinet.service.DataHandlerListenerTest;
import com.diwinet.util.MemCachedManager;

class InitTest {
	private static  Logger log = Logger.getLogger(Listener.class.getName());
	public static void main(String []args) throws Exception {

		MemCachedManager cache = MemCachedManager.getInstance();
		String user = cache.getString("mqtt.user.test");
		String password = cache.getString("mqtt.password.test");
		String host = cache.getString("mqtt.host.test");
		int port = new Integer(cache.getString("mqtt.port.test")).intValue();
		final String destination = cache.getString("mqtt.topic.test");
		boolean flag = true;

		MQTT mqtt = new MQTT();
		mqtt.setHost(host, port);
		mqtt.setUserName(user);
		mqtt.setPassword(password);

//		mqtt.setCleanSession(true);
//		mqtt.setClientId("clienteqwe312");
		//失败重连接设置说明
		mqtt.setConnectAttemptsMax(5L);//客户端首次连接到服务器时，连接的最大重试次数，超出该次数客户端将返回错误。-1意为无重试上限，默认为-1
		mqtt.setReconnectAttemptsMax(-1L);//客户端已经连接到服务器，但因某种原因连接断开时的最大重试次数，超出该次数客户端将返回错误。-1意为无重试上限，默认为-1
		mqtt.setReconnectDelay(5*1000L);//首次重连接间隔毫秒数，默认为10ms
		mqtt.setReconnectDelayMax(1000*3L);//重连接间隔毫秒数，默认为30000ms
		//        mqtt.setReconnectBackOffMultiplier(2);//设置重连接指数回归。设置为1则停用指数回归，默认为2
		mqtt.setReceiveBufferSize(312);//设置socket接收缓冲区大小，默认为65536（64k）
		mqtt.setSendBufferSize(123);//设置socket发送缓冲区大小，默认为65536（64k）
		mqtt.setTrafficClass(8);//设置发送数据包头的流量类型或服务类型字段，默认为8，意为吞吐量最大化传输
		//带宽限制设置说明
		mqtt.setMaxReadRate(0);//设置连接的最大接收速率，单位为bytes/s。默认为0，即无限制
		mqtt.setMaxWriteRate(0);//设置连接的最大发送速率，单位为bytes/s。默认为0，即无限制

		final CallbackConnection connection = mqtt.callbackConnection();

		connection.listener(new DataHandlerListenerTest());

		connection.connect(new Callback<Void>() {
			public void onSuccess(Void value) {
				Topic[] topics = {new Topic(destination, QoS.AT_LEAST_ONCE)};
				connection.subscribe(topics, new Callback<byte[]>() {
					public void onSuccess(byte[] qoses) {
						log.info(Arrays.toString(qoses));
					}
					public void onFailure(Throwable value) {
						log.info("error:"+value.getMessage());
						connection.disconnect(null);
					}
				});
			}
			public void onFailure(Throwable value) {
				log.info("error:"+value.getMessage());
			}
		});
		// Wait forever..
		synchronized (Listener.class) {
			while(flag)
				Listener.class.wait();
		}
	}
}
