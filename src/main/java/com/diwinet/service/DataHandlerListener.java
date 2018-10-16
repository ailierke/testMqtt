package com.diwinet.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Listener;

public class DataHandlerListener implements Listener{
	
	private static  Logger log = Logger.getLogger(Listener.class.getName());
	private ExecutorService executor = Executors.newFixedThreadPool(15);
	public void onConnected() {
		 log.info("connected！！");
	}

	public void onDisconnected() {
		 log.info("connect valid！！");
	}

	public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
		executor.submit(new DiwinetReceivehandler(ack,body,19001) );
	}

	public void onFailure(Throwable value) {
	}

}
