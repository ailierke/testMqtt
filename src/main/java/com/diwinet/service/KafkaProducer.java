package com.diwinet.service;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.diwinet.util.MemCachedManager;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
/**
 * <p>标题：kafka生产者客户端</p>
 * <p>描述：</p>
 * <p>Copyright：Copyright(c) 2016 diwinet</p>
 * <p>日期：2016年5月25日</p>
 * @author	Jack Lee
 */
public class KafkaProducer{

	private static String topic = null;
	private  Producer<Integer, String> producer;
	private volatile static KafkaProducer kafkaProducer;
	private MemCachedManager cache = MemCachedManager.getInstance();
	Logger logger = Logger.getLogger(this.getClass().getName());



	private static Properties props = null;
	private KafkaProducer(){}   
	public static KafkaProducer getInstace() {  
		if (kafkaProducer == null) {  
			synchronized (KafkaProducer.class) {  
				if (kafkaProducer == null) {  
					kafkaProducer = new KafkaProducer();
					kafkaProducer.init();
				}  
			}  
		}  
		return kafkaProducer;  
	}  


	private  void init(){
		String brokerListUrl =cache.getString("metadata.broker.list");
		topic = cache.getString("kafka.topic");
		//		topic ="DW-TOPIC-MSG";
		//		topic ="data";
		//		String brokerListUrl = "192.168.0.53:9092,192.168.0.53:9093";
		//		String brokerListUrl = "101.201.104.132:9092,101.201.104.132:9093";
		props = new Properties();
		//序列化编码
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("message.send.max.retries","1");
		props.put("request.required.acks", "1");
		//节点列表url
		props.put("metadata.broker.list", brokerListUrl);
		producer =  new Producer<Integer, String>(new ProducerConfig(props));
	}
	/**
	 * <p>说明：放入消息到kafka队列</p>
	 * <p>时间：2015年9月22日 下午4:53:56</p>
	 * @param msgStr
	 */
	public void transfer(String data){
		//把消息放入kafka队列
		producer.send(new KeyedMessage<Integer, String>(topic, data));
		logger.info("send success:"+data);
	}

	public void close(){
		producer.close();
	}
}
