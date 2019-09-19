package org.embest.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogUtils {

	public static Logger getLogger(String name) {
		Logger logger = Logger.getLogger(name);
		FileHandler handler = null; 
		File file = new File(System.getProperty("user.dir") +"/logs");
		if (!file.exists()) {
            file.mkdirs();
        }
		try { 
			handler = new FileHandler(file.getAbsolutePath()+"/mesh.log",1000000,10,true);
		} catch (IOException e) { 
			logger.severe("文件夹不存在"); 
		}
		logger.addHandler(handler);
		logger.setLevel(Level.INFO);
		SimpleFormatter formatter = new SimpleFormatter();
		handler.setFormatter(formatter);
		return logger;
	}
}
