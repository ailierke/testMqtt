package testmqtt;

import org.fusesource.mqtt.client.Callback;

public class MqttPushCallBack implements Callback<Void> {
	public MqttPushCallBack(String msg) {
		super();
		this.msg = msg;
	}

	private String msg;
	@Override
	public void onSuccess(Void v) {
		System.out.println("publish success 【"+msg+"】");
	}
	@Override
	public void onFailure(Throwable value) {
		System.out.println("发布失败"+value.getMessage());
	}
	
 }  