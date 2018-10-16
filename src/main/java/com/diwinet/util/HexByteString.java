package com.diwinet.util;
public class HexByteString {
	/**
	 * 用于建立十六进制字符的输出的小写字符数组
	 */
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	/**
	 * 用于建立十六进制字符的输出的大写字符数组
	 */
	private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	/**
	 * 将字节数组转换为十六进制字符数组
	 *
	 * @param data
	 *            byte[]
	 * @return 十六进制char[]
	 */
	public static char[] encodeHex(byte[] data) {
		return encodeHex(data, true);
	}
	/**
	 * 将字节数组转换为十六进制字符数组
	 *
	 * @param data
	 *            byte[]
	 * @param toLowerCase
	 *            <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
	 * @return 十六进制char[]
	 */
	public static char[] encodeHex(byte[] data, boolean toLowerCase) {
		return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
	}
	/**
	 * 将字节数组转换为十六进制字符数组
	 *
	 * @param data
	 *            byte[]
	 * @param toDigits
	 *            用于控制输出的char[]
	 * @return 十六进制char[]
	 */
	protected static char[] encodeHex(byte[] data, char[] toDigits) {
		int l = data.length;
		char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
			out[j++] = toDigits[0x0F & data[i]];
		}
		return out;
	}
	/**
	 * 将字节数组转换为十六进制字符串
	 *
	 * @param data
	 *            byte[]
	 * @return 十六进制String
	 */
	public static String encodeHexStr(byte[] data) {
		return encodeHexStr(data, true);
	}
	/**
	 * 将字节数组转换为十六进制字符串
	 *
	 * @param data
	 *            byte[]
	 * @param toLowerCase
	 *            <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
	 * @return 十六进制String
	 */
	public static String encodeHexStr(byte[] data, boolean toLowerCase) {
		return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
	}
	/**
	 * 将字节数组转换为十六进制字符串
	 *
	 * @param data
	 *            byte[]
	 * @param toDigits
	 *            用于控制输出的char[]
	 * @return 十六进制String
	 */
	protected static String encodeHexStr(byte[] data, char[] toDigits) {
		return new String(encodeHex(data, toDigits));
	}
	/**
	 * 将十六进制字符数组转换为字节数组
	 *
	 * @param data
	 *            十六进制char[]
	 * @return byte[]
	 * @throws RuntimeException
	 *             如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
	 */
	public static byte[] decodeHex(char[] data) {
		int len = data.length;
		if ((len & 0x01) != 0) {
			throw new RuntimeException("Odd number of characters.");
		}
		byte[] out = new byte[len >> 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; j < len; i++) {
			int f = toDigit(data[j], j) << 4;
			j++;
			f = f | toDigit(data[j], j);
			j++;
			out[i] = (byte) (f & 0xFF);
		}
		return out;
	}
	/**
	 * 将十六进制字符转换成一个整数
	 *
	 * @param ch
	 *            十六进制char
	 * @param index
	 *            十六进制字符在字符数组中的位置
	 * @return 一个整数
	 * @throws RuntimeException
	 *             当ch不是一个合法的十六进制字符时，抛出运行时异常
	 */
	protected static int toDigit(char ch, int index) {
		int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new RuntimeException("Illegal hexadecimal character " + ch
					+ " at index " + index);
		}
		return digit;
	}



	/**  
	 * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用 
	 * @param value  
	 *            要转换的int值 
	 * @return byte数组 
	 */    
	public static byte[] intToBytes( int value )   
	{   
		byte[] src = new byte[4];  
		src[3] =  (byte) ((value>>24) & 0xFF);  
		src[2] =  (byte) ((value>>16) & 0xFF);  
		src[1] =  (byte) ((value>>8) & 0xFF);    
		src[0] =  (byte) (value & 0xFF);                  
		return src;   
	}  
	/**  
	 * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用 
	 */    
	public static byte[] intToBytes2(int value)   
	{   
		byte[] src = new byte[4];  
		src[0] = (byte) ((value>>24) & 0xFF);  
		src[1] = (byte) ((value>>16)& 0xFF);  
		src[2] = (byte) ((value>>8)&0xFF);    
		src[3] = (byte) (value & 0xFF);       
		return src;  
	}
	/**  
	 * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用 
	 *   
	 * @param src  
	 *            byte数组  
	 * @param offset  
	 *            从数组的第offset位开始  
	 * @return int数值  
	 */    
	public static int bytesToInt(byte[] src, int offset) {  
		int value;    
		value = (int) ((src[offset] & 0xFF)   
				| ((src[offset+1] & 0xFF)<<8)   
				| ((src[offset+2] & 0xFF)<<16)   
				| ((src[offset+3] & 0xFF)<<24));  
		return value;  
	}  

	/**  
	 * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用 
	 */  
	public static int bytesToInt2(byte[] src, int offset) {  
		int value;    
		value = (int) ( ((src[offset] & 0xFF)<<24)  
				|((src[offset+1] & 0xFF)<<16)  
				|((src[offset+2] & 0xFF)<<8)  
				|(src[offset+3] & 0xFF));  
		return value;  
	}  
	
	
	
	
	public static void main(String[] args) {
		//    	byte[] b = new byte[]{70, 48, 70, 69, 50, 53, 54, 55, 48, 57, 48, 49, 53, 48, 53, 55, 56, 48, 48, 56, 56, 48, 50, 49, 57, 54, 49, 50, 52, 50, 49, 50, 50, 51, 48, 50, 49, 48, 49, 50};
		//    	byte[] b = new byte[]{-16, -2, 49, 95, -98, -60, 45, 31, -62, -119, -17, 1, 0, 1, 4, 0, 0, 0, 12, 0, 2, 4, 0, 0, 0, 3, 0, 3, 4, 0, 0, 0, 2, 0, 4, 4, 0, 0, 0, 2, 0, 5, 1, 3, 0, 6, 1, 1, -91};
		byte[] b = new byte[]{123, 1, 0, 22, 49, 53, 50, 53, 49, 53, 51, 54, 57, 50, 51, 10, 28, 30, -57, 19, -119, 123};
		System.out.println(new String(b));
		System.out.println(encodeHexStr(b,false));
		String srcStr = "待转换字符串";
		String encodeStr = encodeHexStr(srcStr.getBytes());
		String decodeStr = new String(decodeHex(encodeStr.toCharArray()));
		System.out.println("转换前：" + srcStr);
		System.out.println("转换后：" + encodeStr);
		System.out.println("还原后：" + decodeStr);
	}


}