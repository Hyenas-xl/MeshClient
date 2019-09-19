package org.embest.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class TaskUtil {
	private static final Logger logger = LogUtils.getLogger("TaskUtil");
	private ExecutorService executorService;
	private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue <>();
	
	public static final TaskUtil instance = new TaskUtil();
	
	private TaskUtil(){
		init();
	}
	
	private void init(){
		executorService = Executors.newSingleThreadExecutor();
//		executorService = new ThreadPoolExecutor(2,4,60L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());
	}
	
	public void run(){
		while(true){
			try {
				Runnable command = queue.take();
				if(command != null){
					executorService.execute(command);
				}
			} catch (InterruptedException e) {
				logger.severe("Task util take task failed: "+ e.getMessage());
			}
		}
	}
	
	public void add(Runnable command) throws IllegalStateException{
		queue.add(command);
	}
}
