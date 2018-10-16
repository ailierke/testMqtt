package com.diwinet.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.fusesource.hawtbuf.Buffer;

import com.alibaba.fastjson.JSON;
import com.diwinet.util.DataReverse;
import com.diwinet.util.HttpClientUtils;
import com.diwinet.util.MemCachedManager;


public class DiwinetReceivehandler implements Runnable{
	private Logger logger  = Logger.getLogger(this.getClass().getName());
	private String HEARTRATE_KEY = "";
	private Runnable ack;
	private Buffer data;
	private MemCachedManager cache =MemCachedManager.getInstance();
	private String url = cache.getString("service.send.command.url");

	public DiwinetReceivehandler(int port){
		this.HEARTRATE_KEY = "MEM_DW_STATUS_"+port;
	}

	@Override
	public void run() {
		//业务逻辑处理
		System.out.println(data);
//		String body = data.hex().toLowerCase().toString();
		String body =  data.hex().toLowerCase().toString();
		if(body.startsWith("f0fe")){	
			try {
				logger.info("收取报送水量数据【"+body+"】");
				dataDeal(body);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}else{
			try {
				body = data.utf8().toString();
				logger.info("收取命令反馈数据【"+body+"】");
				@SuppressWarnings("unchecked")
				Map<String,Object> data = JSON.parseObject(body,Map.class);
				Long sbtm = Long.parseLong((String)data.get("sbtm"), 16);
				Map<String,String> params = new HashMap<String,String>();
				params.put("v", (String)data.get("v"));
				params.put("k",data.get("k").toString());
				params.put("sbtm",sbtm.toString());
				params.put("wllx", "0");//网络类型 0、wifi,1、gprs 
				//调用远程数据
				String  back = HttpClientUtils.simplePostInvoke(url, params);
				logger.info("发送反馈信息"+back);
			} catch (Exception e) {
				logger.info("********** ERROR:  packet is illegal\t" );
				e.printStackTrace();
			}
		}
		ack.run();
	}
	public DiwinetReceivehandler(Runnable ack,Buffer data,int port) {
		super();
		this.ack = ack;
		this.data = data;
		this.HEARTRATE_KEY = "MEM_DW_STATUS_"+port;
	}
	public Runnable getAck() {
		return ack;
	}
	public void setAck(Runnable ack) {
		this.ack = ack;
	}
	public Buffer getData() {
		return data;
	}
	public void setData(Buffer data) {
		this.data = data;
	}

	/**
	 * <p>说明：收取数据进行处理</p>
	 * <p>时间：2016年5月30日 上午10:01:46</p>
	 * @param body
	 * @throws IOException
	 */
	private void dataDeal(String body) throws IOException {
		// 获取调试模式
		int debug = 1;
		String data[] = this.parseReceivedNew(body);
		if(data==null){
			logger.info("********** exception: data is illegal\t");
		}
		if(isLegal(data[2])){
			if(data[3].equals("01")){//设备报送
				/**
				 * 数据处理
				 */
				opreateRealData(data,debug);//数据处理
			}else if(data[3].equals("03")){//设备应答
				//wifi模块不需要这个模块
			}
		}else{
			logger.info("sbtm is illege");
		}
	}

	private void opreateRealData(String[] data,Integer debug) {
		Map<String,Object> dataMap = null;
		dataMap = this.getDataOrHeartBate(data,debug);
		if(dataMap!=null){
			// 判断条码是否合法
			dataMap.put("time", System.currentTimeMillis());

			dataMap.put("tbsj", new Date(System.currentTimeMillis()));
			/**
			 * 数据理
			 */
			try {
				String transfer = JSON.toJSONString(dataMap);
				KafkaProducer.getInstace().transfer(transfer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			logger.info("data is null,do nothing");
		}		
	}
	/**
	 * <p>说明：获取放入缓存的对象/放入、更新心跳缓存数据</p>
	 * <p>时间：2015年4月21日 下午7:32:13</p>
	 * @param data 解析结果
	 */
	private Map<String,Object> getDataOrHeartBate(String[] data,Integer debug){
		Map<String,Object> historyMap = null;
		Map<String,Object> heartRate = null;
		try {
			StringBuilder stringBuilder = new StringBuilder(130);
			// 获取设备条码
			Long sbtm = Long.valueOf(Long.parseLong(data[2], 16));
			// 设备条码校验

			Integer yssl = (data[4]==null)?null:Integer.valueOf(Integer.parseInt(data[4], 16));
			Integer cssl = (data[5]==null)?null:Integer.valueOf(Integer.parseInt(data[5], 16));
			Integer yssz = (data[6]==null)?null:Integer.valueOf(Integer.parseInt(data[6], 16));
			Integer cssz = (data[7]==null)?null:Integer.valueOf(Integer.parseInt(data[7], 16));
			Integer ysls = (data[8]==null)?null:Integer.valueOf(Integer.parseInt(data[8], 16));
			Integer csls = (data[9]==null)?null:Integer.valueOf(Integer.parseInt(data[9], 16));
			String iccid = DataReverse.AsciiStringToString(data[10]);//iccid
			Long sysj = (data[11]==null)?null:Long.valueOf(Long.parseLong(data[11], 16));//时间
			Long jsjzt = (data[12]==null)?null:Long.valueOf(Long.parseLong(data[12], 16));//时间
			Date dt = new Date();
			SimpleDateFormat t = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			// 打印解析数据
			if(debug > 0){
				stringBuilder.append("sbtm：").append(sbtm).append("\ttime：").append(t.format(dt));
				stringBuilder.append("\tdata:{"+yssl).append(","+cssl+"}");
				stringBuilder.append("{"+yssz).append(","+cssz+"}");
				stringBuilder.append("{"+ysls).append(","+csls+"}");
				stringBuilder.append("{"+iccid).append(","+sysj+"}");
				stringBuilder.append("{"+jsjzt+"}");
				logger.info(stringBuilder.toString());
			}
			Integer wllx = 0;//0.wifi  1、gprs通过MQTT服务 全是wifi模块
			if(sysj!=null){
				//租赁式设备
				if(isSimZero(sysj)){
					logger.info("heart beat package......");
					heartRate = new HashMap<String,Object>();
					heartRate.put(sbtm.toString(), System.currentTimeMillis());
					cacheHeartRate(heartRate,sbtm.toString());
				}else{
					historyMap = new HashMap<String,Object>();
					historyMap.put("sbtm", sbtm.toString());
					historyMap.put("iccid", iccid);
					historyMap.put("sysj", sysj);
					historyMap.put("bssj", dt);
					historyMap.put("wllx", wllx);
				}
			}else{
				//正常设备
				if(isZero(yssl, cssl, yssz, cssz, ysls, csls)){
					heartRate = new HashMap<String,Object>();
					heartRate.put(sbtm.toString(), System.currentTimeMillis());
					cacheHeartRate(heartRate,sbtm.toString());
				}else{
					historyMap = new HashMap<String,Object>();
					historyMap.put("sbtm", sbtm.toString());
					historyMap.put("yssl", yssl);
					historyMap.put("cssl", cssl);
					historyMap.put("yssz", yssz);
					historyMap.put("cssz", cssz);
					historyMap.put("ysls", ysls);
					historyMap.put("csls", csls);
					historyMap.put("iccid", iccid);
					historyMap.put("sysj", sysj);
					historyMap.put("bssj", dt);
					historyMap.put("wllx", wllx);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			historyMap = null;
			logger.info("className:【"+this.getClass().getName()+"】methodName:【getDataOrHeartBate】Parsing exceptions");
		}
		return  historyMap;
	}

	/**
	 * <p>
	 * 		说明:1、存入设备报送上来的心跳
	 * 			2、存入当前净水机的状态
	 * </p>
	 * <p>时间：2015年8月13日 下午1:20:42</p>
	 * @param history
	 */
	private void cacheHeartRate(Map<String,Object> heartRates,String sbtm){
		//1、存入设备报送上来的心跳
		if(cache.keyExists(HEARTRATE_KEY)){
			@SuppressWarnings("unchecked")
			Map<String,Object> cacheHeartRates = (Map<String, Object>) cache.get(HEARTRATE_KEY);
			if(cacheHeartRates!=null){
				cacheHeartRates.putAll(heartRates);
			}
			cache.set(HEARTRATE_KEY, cacheHeartRates , 0);
		}else{
			cache.set(HEARTRATE_KEY, heartRates , 0);
		}
		//2、存入当前净水机的状态

	}

	public String[] parseReceivedNew(String received ) {
		boolean isNullArray = false;
		String[] data = new String[13];
		Long packageLen = 0L;
		try {
			String rev = received;
			data[0] = rev.substring(0, 4);//head
			data[1] = rev.substring(4, 6);//整包长度
			data[2] =rev.substring(6, 22);//设备 
			if (data[2].length() != 16) {
				isNullArray = true;
			} else {
				data[3] = rev.substring(22, 24);
				//设备上报数据
				if (data[3].equals("01")) {
					packageLen = Long.parseLong(data[1], 16);
					String tlv  = null;
					//匹配gprs接收数据报送
					int tlvsLength = (packageLen.intValue()-13)*2;//包长度减去固定的13字节
					int tlvsStart = 24;//tlvs起始地址
					tlv = rev.substring(tlvsStart, tlvsStart+tlvsLength);
					int realPoint = 0;
					Long cmdLen = null;
					int tagType = 0;
					while(realPoint<tlvsLength){
						tagType = Integer.parseInt(tlv.substring(realPoint,realPoint+4),16);//tag类别
						cmdLen = Long.parseLong(tlv.substring(realPoint+4,realPoint+6), 16)*2;
						switch (tagType) {
						case 1:
							data[4] =tlv.substring(realPoint+6,realPoint+6+cmdLen.intValue());
							break;
						case 2:
							data[5] = tlv.substring(realPoint+6,realPoint+6+cmdLen.intValue());
							break;
						case 3:
							data[6] = tlv.substring(realPoint+6,realPoint+6+cmdLen.intValue());
							break;
						case 4:
							data[7] = tlv.substring(realPoint+6,realPoint+6+cmdLen.intValue());
							break;
						case 5:
							data[8] = tlv.substring(realPoint+6,realPoint+6+cmdLen.intValue());
							break;
						case 6:
							data[9] = tlv.substring(realPoint+6,realPoint+6+cmdLen.intValue());
							break;
						case 7:
							data[10] = tlv.substring(realPoint+6,realPoint+6+cmdLen.intValue());//ssid
							break;
						case 8:
							data[11] = tlv.substring(realPoint+6,realPoint+6+cmdLen.intValue());//计时值
							break;
						case 9:
							data[12] = tlv.substring(realPoint+6,realPoint+6+cmdLen.intValue());//净水机状态
							break;
						default:
							break;
						}
						realPoint = realPoint+6+cmdLen.intValue();
					}
				} else if(data[3].equals("03")){//设备应答
					//应答直接走mqtt协议主题
				}else{
					isNullArray = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			isNullArray = true;
		}
		if (isNullArray) {
			data = null;
		}
		return data;
	}




	/**
	 * <p>说明：验证条形编码是否合法</p>
	 * <p>时间：2015年2月4日 下午2:25:41</p>
	 * @param sbtm 设备条码
	 * @return 合法状态
	 */
	public  boolean  isLegal(String sbtm){
		boolean txbm = false;
		try {
			sbtm = Long.valueOf(sbtm, 16).toString();
			if (sbtm.length() == 19) {
				String a = sbtm.substring(0, 2);
				int tmv = Integer.parseInt(a);
				if (tmv >= 65 && tmv <= 70) {
					a = sbtm.substring(2, 4);
					tmv = Integer.parseInt(a);
					if (tmv == 90) {
						a = sbtm.substring(4, 6);
						tmv = Integer.parseInt(a);
						if (tmv >= 14 && tmv <= 30) {
							a = sbtm.substring(6, 8);
							tmv = Integer.parseInt(a);
							if (tmv > 0 && tmv <= 12) {
								a = sbtm.substring(8, 10);
								if (Integer.parseInt(a) == 78) {
									a = sbtm.substring(12, 14);
									if (Integer.parseInt(a) == 88) {
										a = sbtm.substring(18, 19);
										if (Integer.parseInt(a) == getLastCode(sbtm)) {
											txbm = true;
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			txbm = false;
		}
		return txbm;
	}
	/**
	 * <p>说明：计算水管家条码最后一位</p>
	 * <p>时间：2014年12月17日 下午12:33:05</p>
	 * @param txt 条码
	 * @return 校验条码结果
	 */
	private int getLastCode(String txt) {
		char[] a = txt.toCharArray();
		a[txt.length() - 1] = '0';
		char[] b = new char[a.length];
		int temp = 0;
		for (int j = a.length - 1; j >= 0; j--) {
			b[temp] = a[j];
			temp++;
		}
		int sumOdd = 0;
		int sumEven = 0;
		for (int i = 0; i < b.length; i++) {
			if ((i) % 2 == 0) {
				sumOdd += (b[i] - '0');
			} else {
				sumEven += (b[i] - '0');
			}
		}
		int c = sumEven * 3 + sumOdd;
		int num = c % 10;
		if (num == 0) {
			num = 0;
		} else {
			num = 10 - num;
		}
		return num;
	}

	/**
	 * <p>说明：判断是否是设备报送的心跳信息</p>
	 * <p>时间：2015年8月13日 下午1:53:15</p>
	 * @param yssl @param cssl  @param yssz
	 * @param cssz @param ysls  @param csls
	 * @return
	 */
	private boolean isZero(Integer yssl,Integer cssl,Integer yssz,Integer cssz,Integer ysls,Integer csls){
		
		boolean flag = false;
		if(yssl==null&&cssl==null&&yssz==null&&cssz==null&&ysls==null&&csls==null){
			return true;
		}
		if(yssl!=null&&cssl!=null&&yssz!=null&&cssz!=null&&ysls!=null&&csls!=null){
			if(yssl.intValue()==0&&cssl.intValue()==0&&yssz.intValue()==0&&cssz.intValue()==0&&ysls.intValue()==0&&csls.intValue()==0){
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * <p>说明：判断是iccid、sysj的心跳信息</p>
	 * <p>时间：2015年8月13日 下午1:53:15</p>
	 * @param yssl @param cssl  @param yssz
	 * @param cssz @param ysls  @param csls
	 * @return
	 */
	private boolean isSimZero(Long sysj){
		boolean flag = false;
		if(sysj!=null){
			if(sysj.intValue()==0){
				flag = true;
			}
		}
		return flag;
	}
}
