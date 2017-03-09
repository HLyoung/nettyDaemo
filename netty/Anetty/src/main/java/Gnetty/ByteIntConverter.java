package Gnetty;

public class ByteIntConverter {

	public static void convertInt2ByteArray(byte[] b, int start, int nValue) {
		b[start] = (byte) (nValue & 0x000000ff);
		b[start + 1] = (byte) ((nValue & 0x0000ff00) >> 8);
		b[start + 2] = (byte) ((nValue & 0x00ff0000) >> 16);
		b[start + 3] = (byte) ((nValue & 0xff000000) >> 24);
	}

	public static int convertByteArray2Int(byte[] b, int s) {
		int nR = 0;
		nR = b[s] & 0xff;
		nR = nR + (b[s + 1] << 8 & 0x0000ff00);
		nR = nR + (b[s + 2] << 16 & 0x00ff0000);
		nR = nR + (b[s + 3] << 24 & 0xff000000);
		return nR;
	}

	/**
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		// hexString = hexString.toUpperCase(); //如果是大写形式
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789abcdef".indexOf(c);
		// return (byte) "0123456789ABCDEF".indexOf(c);
	}
}
