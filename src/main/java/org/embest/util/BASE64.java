package org.embest.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Created by xiele on 2019/8/28.
 */
public class BASE64 {
	private static final Logger logger = LogUtils.getLogger("BASE64");
	private static BASE64Encoder encoder = new BASE64Encoder();
	private static BASE64Decoder decoder = new BASE64Decoder();

    public static String decryptBASE64(String content){
		try {
			byte[] decoded = decoder.decodeBuffer(content);
//	        byte[] decoded = Base64.getDecoder().decode(content);
			decoded[0] = decoded[decoded.length-1];
	        byte[] scores = Arrays.copyOf(decoded, decoded.length - 1);
	        byte[] scores2 = decoder.decodeBuffer(new String(scores));
	        return new String(scores2);
		} catch (IOException e) {
			logger.severe("Decrypt failed: " + content);
			return null;
		}
    }

    public static String encryptBASE64(String content){
    	String value = encoder.encodeBuffer(content.getBytes());
        byte[] decoded = value.getBytes();
        byte[] scores = Arrays.copyOf(decoded, decoded.length + 1);
        scores[decoded.length] = decoded[0];
        scores[0] = decoded[decoded.length-1];
        return encoder.encodeBuffer(scores);
    }
}
