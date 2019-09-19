package org.embest.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

public class FileUtils {
	private static final Logger logger = LogUtils.getLogger(FileUtils.class.getName());
	
	public static String read(String path) {
        InputStreamReader reader = null;
        BufferedReader bufReader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(path));
            bufReader = new BufferedReader(reader);
            String line = "";
            String result = "";
            while ((line = bufReader.readLine()) != null)
                result += line;
            return result;
        } catch (IOException e) {
        	logger.severe(e.getMessage());
        } catch (Exception e) {
        	logger.severe(e.getMessage());
        } finally {
            try {
                if (bufReader != null){
                    bufReader.close();
                }
                if (bufReader != null){
                    bufReader.close();
                }
            } catch (IOException e) {
            	logger.severe(e.getMessage());
            } catch (Exception e){
            	logger.severe(e.getMessage());
            }
        }
        return null;
    }
	
	public static void write(String path,String data){
		OutputStreamWriter out = null;
		BufferedWriter bufWrite = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(path));
			bufWrite = new BufferedWriter(out);
			bufWrite.write(data);
		} catch (IOException e) {
			logger.severe(e.getMessage());
		} catch (Exception e) {
			logger.severe(e.getMessage());
		} finally {
			try{
				if(bufWrite != null){
					bufWrite.close();
				}
				if(bufWrite != null){
					bufWrite.close();
				}
			} catch (IOException e) {
				logger.severe(e.getMessage());
            } catch (Exception e){
            	logger.severe(e.getMessage());
            }
		}
	}

}
