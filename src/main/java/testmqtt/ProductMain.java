package testmqtt;


public class ProductMain {
	public static void main(String[] args) {
			try {
				new Thread(new PublisherCallBackConnection(1)).start();
				while(true){}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
