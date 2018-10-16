package testmqtt;


import java.util.Arrays;
import java.util.logging.Logger;

import org.fusesource.hawtbuf.*;
import org.fusesource.mqtt.client.*;

/**
 * Uses an callback based interface to MQTT.  Callback based interfaces
 * are harder to use but are slightly more efficient.
 */
class Listener3 {
	private static  Logger log = Logger.getLogger(Listener3.class.getName());
    public static void main(String []args) throws Exception {
        String user = env("APOLLO_USER", "admin");
        String password = env("APOLLO_PASSWORD", "password");
        String host = env("APOLLO_HOST", "net.d-water.cn");
        int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
        final String destination = arg(args, 0, "diwinet/test");
        

        MQTT mqtt = new MQTT();
        mqtt.setHost(host, port);
        mqtt.setUserName(user);
        mqtt.setPassword(password);
        mqtt.setClientId("test1");
        mqtt.setCleanSession(true);

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
                System.out.println(body);
            }
        });
        connection.connect(new Callback<Void>() {
            public void onSuccess(Void value) {
                Topic[] topics = {new Topic(destination, QoS.AT_LEAST_ONCE)};
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
        synchronized (Listener3.class) {
            while(true)
                Listener3.class.wait();
        }
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
