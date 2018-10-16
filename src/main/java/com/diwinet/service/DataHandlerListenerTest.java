package com.diwinet.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Listener;

import com.diwinet.util.DataReverse;
import com.diwinet.util.HexByteString;

public class DataHandlerListenerTest implements Listener{
	
	private static  Logger log = Logger.getLogger(Listener.class.getName());
	private ExecutorService executor = Executors.newFixedThreadPool(15);
	public void onConnected() {
		 log.info("connected！！");
	}

	public void onDisconnected() {
		 log.info("connect valid！！");
	}

	public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
		System.out.println(body);
	}

	public void onFailure(Throwable value) {
	}

}
