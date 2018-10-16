package com.diwinet.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

/**
 * <p>标题：MemCachedManager管理类</p>
 * <p>描述：对Memcached进行缓存管理</p>
 * <p>Copyright：Copyright(c) 2014 diwinet</p>
 * <p>日期：2014-3-10</p>
 * @author	Jack Lee
 */
public class MemCachedManager {

	/**
	 * 创建 MemCachedClient 全局的唯一实例
	 */
	private MemCachedClient client = new MemCachedClient();
	Logger logger = Logger.getLogger(MemCachedManager.class.getName());
	
	public MemCachedClient getClient() {
		return client;
	}

	public void setClient(MemCachedClient client) {
		this.client = client;
	}

	/**
	 * 声明MemCachedManager
	 */
	private static MemCachedManager cache = null;
	private MemCachedManager() {
	}

	/**
	 * <p>说明：单列构建MemCachedManager对象</p>
	 * <p>时间：2014-3-10 下午3:20:50</p>
	 * @return MemCachedManager对象
	 */
	public static MemCachedManager getInstance() {
		if (cache == null) {
			synchronized (MemCachedManager.class) {
				if (cache == null) {
					cache = new MemCachedManager();
					cache.initConnection();
				}
			}
		}
		return cache;
	}
	
	/**
	 * <p>说明：初始化MemCached链接</p>
	 * <p>时间：2014-3-10 下午3:19:41</p>
	 */
	private void initConnection() {
		Properties properties = getProperties();
		String server = properties.getProperty("memcached.servers");
		String port = properties.getProperty("memcached.port");
		String weight = properties.getProperty("memcached.weights");
		
		String[] servers = server.split(",");
		for (int i = 0; i < servers.length; i++) {
			servers[i] = servers[i] + ":" + port;
		}
		
		Integer[] weights = getMemWeights(weight);
		initPool(servers, weights);
		
		properties.clear();
		properties = null;
	}
	/**
	 * <p>说明：初始化连接池</p>
	 * <p>时间：2014-3-10 下午3:20:07</p>
	 * @param properties Properties对象
	 * @param servers 服务器地址数组
	 * @param weights 权重
	 */
	private void initPool(String[] servers, Integer[] weights) {
		// 获取socke连接池的实例对象
		SockIOPool pool = SockIOPool.getInstance();
		// 设置服务器信息
		pool.setServers(servers);
		pool.setWeights(weights);
		// 设置初始连接数、最小和最大连接数以及最大处理时间

		pool.setInitConn(getProperties("memcached.initConn"));
		pool.setMinConn(getProperties("memcached.minConn"));
		pool.setMaxConn(getProperties("memcached.maxConn"));
		pool.setMaxIdle(getProperties("memcached.maxidle"));
		// 设置主线程的睡眠时间
		pool.setMaintSleep(getProperties("memcached.maintSleep"));
		// 设置TCP的参数，连接超时等
		pool.setNagle(false);
		pool.setSocketTO(getProperties("memcached.socketto"));
		pool.setSocketConnectTO(getProperties("memcached.socketConnectto"));
		pool.setHashingAlg(SockIOPool.CONSISTENT_HASH);

		// 初始化连接池
		pool.initialize();
	}

	/**
	 * 添加一个指定的值到缓存中.
	 * @param key key
	 * @param value 存入值
	 * @return 添加状态
	 */
	public boolean set(String key, Object value) {
		return client.set(key, value);
	}

	/**
	 * <p>说明：将值存入缓存中</p>
	 * <p>时间：2014-3-10 下午3:21:36</p>
	 * @param key 指定key
	 * @param value 指定key存入的value
	 * @param minute 有效时间 分钟
	 * @return 存入状态
	 */
	public boolean set(String key, Object value, Integer minute) {
		return client.set(key, value, new Date(minute.longValue() * 60 * 1000));
	}
	/**
	 * <p>说明：将值存入缓存中</p>
	 * <p>时间：2014-3-10 下午3:21:36</p>
	 * @param second 有效时间 秒
	 * @param key 指定key
	 * @param value 指定key存入的value
	 * @return 存入状态
	 */
	public boolean set(Integer second, String key, Object value) {
		return client.set(key, value, new Date(second.longValue() * 1000));
	}
	
	/**
	 * <p>说明：删除缓存</p>
	 * <p>时间：2014-3-10 下午3:22:21</p>
	 * @param key 指定删除缓存Key
	 * @return 删除状态
	 */
	public boolean delete(String key) {
		return client.delete(key);
	}
	
	/**
	 * <p>说明：清除所有</p>
	 * <p>时间：2014-3-12 下午6:01:22</p>
	 * @return 清除状态
	 */
	public boolean flushAll() {
		return client.flushAll();
	}

	/**
	 * <p>说明：更新缓存对象</p>
	 * <p>时间：2014-3-10 下午3:22:48</p>
	 * @param key 指定缓存key
	 * @param value 指定更新缓存值
	 * @return 更新结果,true更新成功  false更新失败
	 */
	public boolean replace(String key, Object value) {
		return client.replace(key, value);
	}
	
	/**
	 * <p>说明：更新缓存对象</p>
	 * <p>时间：2014-3-10 下午3:22:48</p>
	 * @param key 指定缓存key
	 * @param value 指定更新缓存值
	 * @param minute 有效时间 分钟
	 * @return 更新结果,true更新成功  false更新失败
	 */
	public boolean replace(String key, Object value, Integer minute) {
		return client.replace(key, value, new Date(minute.longValue() * 60 * 1000));
	}

	/**
	 * <p>说明：判断Key是否存在</p>
	 * <p>时间：2014-3-10 下午3:24:14</p>
	 * @param key 判断缓存中的key是否存在
	 * @return true 存在  false不存在
	 */
	public boolean keyExists(String key) {
		return client.keyExists(key);
	}

	/**
	 * <p>说明：根据指定key获取对象</p>
	 * <p>时间：2014-3-10 下午3:24:47</p>
	 * @param key 获取对象key
	 * @return 指定对象
	 */
	public Object get(String key) {
		return client.get(key);
	}

	/**
	 * <p>说明：获取配置权重</p>
	 * <p>时间：2014-3-10 下午3:27:09</p>
	 * @param weight 权重字符串
	 * @return 权重数组
	 */
	private Integer[] getMemWeights(String weight) {
		String[] weightArry = weight.split(",");
		Integer[] weights = new Integer[weightArry.length];
		for (int i = 0; i < weightArry.length; i++) {
			weights[i] = Integer.valueOf(weightArry[i]);
		}
		return weights;
	}
	
	/**
	 * <p>说明：获取配置文件</p>
	 * <p>时间：2014-3-10 下午3:27:56</p>
	 * @return 配置文件对象
	 */
	private Properties getProperties(){
		InputStream stream = this.getClass().getClassLoader()
				.getResourceAsStream("config.properties");
		Properties p = new Properties();
		try {
			p.load(stream);
		} catch (IOException e) {
			logger.info("【读取配置文件失败】：" + e.getMessage());
		}
		return p;
	}
	
	
	/**
	 * <p>说明：获取配置文件中指定key对应的value</p>
	 * <p>时间：2014-3-10 下午3:25:42</p>
	 * @param p 配置文件对象
	 * @param key key
	 * @return 数值
	 */
	public int getProperties(String key){
		Properties properties = getProperties();
		return Integer.valueOf(properties.getProperty(key));	
	}
	
	
	/**
	 * <p>说明：获取配置文件中指定key对应的value</p>
	 * <p>时间：2014-3-10 下午3:25:42</p>
	 * @param p 配置文件对象
	 * @param key key
	 * @return 数值
	 */
	public String getString(String key){
		Properties properties = getProperties();
		return properties.getProperty(key);	
	}
	/**
	 * <p>说明：获取监控端口</p>
	 * <p>时间：2014年3月10日 下午9:29:39</p>
	 * @return 返回端口组
	 */
	public String[] getPorts(){
		Properties properties = getProperties();
		String ports = properties.getProperty("dwater.portGroup");
		return ports.split(",");
	}
//	public static void main(String[] args) {
//		String sbtm = "7090150278008813009";
////		System.out.println(Long.valueOf("62654662ae3f6492", 16).toString());
//		// 判断数据是否合法
//		 String  head = "F0FE";
////		 msg.append("14");//整包长度
//		 StringBuilder sb = new StringBuilder();
//		 String hexSbtm = Long.toHexString(new Long(sbtm));
//		 System.out.println(hexSbtm);
//		 sb.append(hexSbtm);//设备条码
//		 sb.append("02");//下发数据
//		 
//		/**
//		 *  0x0001	滤芯状态  0x0002	开关机   0x0003	制水
//		 */
//		 sb.append("0001040a0a0a0a");//过期信息
////		 msg.append("000104141e283c");//过期信息
////		 msg.append("000200");//过期信息  0：开机；1：关机   开关机
////		 msg.append("000301");//0：待机；1：制水    制水
//		 StringBuilder sb1 = new StringBuilder();
//		 sb1.append(head).append(sb);
//		 Integer allLength = HexByteString.decodeHex(sb1.toString().toCharArray()).length+2;
//		 System.out.println("整包长度为："+Integer.toHexString(allLength)+" 个字节");
//		 sb1.delete(0, sb1.length());
//		 sb1.append(head).append(Integer.toHexString(allLength)).append(sb);
//		 System.out.println("发送数据："+sb1.toString());
//		 //计算校验和
//		byte[] b =  HexByteString.decodeHex(sb1.toString().toCharArray());
//		Long total = 0l;
//		for (int i = 0; i < b.length; i++){
//			total += b[i];
//		}
//		 System.out.println(total);
//		 String crc = Long.toHexString(total);
//		 System.out.println("校验和为："+crc);
//		 sb1.append(crc);//校验和
//		 System.out.println("发送数据："+sb1.toString());
////		 System.out.println(0Xf0+0Xfe+0X14+0X62+0X65+0X46+0X1c+0Xd4+0Xda+0X35+0Xd1+0X01+0X00+0X01+0X04+0X00+0X00+0X00+0X00);
////		 f0+fe+14+62+65+46+1c+d4+da+35+d1+02+00+01+04+14+1e+28+3c
////		 F0 FE 14 62 65 46 1c d4 da 35 d1 02 00 01 04 00 00 00 00
//		Long jyh =  Long.valueOf((Long.parseLong("f0", 16))+Long.valueOf(Long.parseLong("fe", 16))+
//		 Long.valueOf(Long.parseLong("14", 16))+
//		 Long.valueOf(Long.parseLong("62", 16))+
//		 Long.valueOf(Long.parseLong("65", 16))+
//		 Long.valueOf(Long.parseLong("46", 16))+
//		 Long.valueOf(Long.parseLong("1c", 16))+
//		 Long.valueOf(Long.parseLong("d4", 16))+
//		 Long.valueOf(Long.parseLong("da", 16))+
//		 Long.valueOf(Long.parseLong("35", 16))+
//		 Long.valueOf(Long.parseLong("d1", 16))+
//		 Long.valueOf(Long.parseLong("02", 16))+
//		 Long.valueOf(Long.parseLong("00", 16))+
//		 Long.valueOf(Long.parseLong("01", 16))+
//		 Long.valueOf(Long.parseLong("04", 16))+
////		 Long.valueOf(Long.parseLong("14", 16))+
////		 Long.valueOf(Long.parseLong("1e", 16))+
////		 Long.valueOf(Long.parseLong("28", 16))+
//// 		 Long.valueOf(Long.parseLong("3c", 16)));
//		 Long.valueOf(Long.parseLong("0a", 16))+
//		 Long.valueOf(Long.parseLong("0a", 16))+
//		 Long.valueOf(Long.parseLong("0a", 16))+
// 		 Long.valueOf(Long.parseLong("0a", 16)));
//		 System.out.println("双位校验和为："+Long.toHexString(jyh));
//		 
//		 MemCachedManager m = MemCachedManager.getInstance();
//		 boolean f =  m.set("command7090150278008813009", "f0fe146265461cd4da35d10200010401000000e7", 0);
//		 System.out.println(f);
//		 System.out.println(m.get("command7090150278008813009"));
//		 
//	}
}
