package testmqtt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductMain2 {
	public static void main(String[] args) {
		ExecutorService executorService  = Executors.newFixedThreadPool(7);
		for(int i = 70000;i<80000;i++){
			try {
				Thread.sleep(20);
				new Thread(new PublisherCallBackConnection2(i)).start();;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		}
			
		}
		while(true){}
	}
}
