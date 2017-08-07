package com.productiveAnalytics.rabbitmq.mutlitaskQueue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.productiveAnalytics.rabbitmq.AMQPConnectionUtility;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class TaskReceiverApp {
	
	private static final int PREFETCH_COUNT = 1;
	
	private static SimpleDateFormat FORMAT_yyyymmdd_hhmmss = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS a");
	
	private TaskReceiverApp() {
		// do not allow to instantiate
	}
	
	public static void main(String[] args)
					   throws InterruptedException
	{
		int max_worker_count = 5;
		
		/*
		 * 
		 * Round Robin means RabbitMQ will send message to Consumer even if Consumer is busy processing earlier message(s).
		 * Instead of Round Robin, improve the performance of overall message processing by using the basicQos method with the prefetchCount = 1 setting.
		 * 
		 */
		boolean round_robin_flag = false;
		
		// Time to live in milliseconds
		int TTL = 60 * 1000; // Default : 1 minutes
		
		if (args.length > 0) {
			max_worker_count = Integer.parseInt(args[0]);
			
			if (args.length > 1) {
				String roundRobin = args[1];
				round_robin_flag = Boolean.valueOf(roundRobin);
				
				if (args.length > 2) {
					int ttl_secs = Integer.parseInt(args[2]); 
					TTL = ttl_secs * 1000;
				}
			}
		}
		
		System.out.println(">>>Consumers starting at "+ FORMAT_yyyymmdd_hhmmss.format(new Date()));
		
		System.out.println("[DEBUG] Round Robin="+ round_robin_flag);
		System.out.println("[DEBUG] TimtToLive ="+ TTL +"ms");
		
		List<Runnable> runnableWorkers = new ArrayList<Runnable>(max_worker_count);
		
		int workerId = 1;
		
		for ( ; workerId <= max_worker_count; ++workerId) {
			runnableWorkers.add(new TaskWorker(workerId, round_robin_flag, TTL));
		}
		
		System.out.println("******************************************************");
		System.out.println("Starting "+ max_worker_count +" worker/consumer threads");
		System.out.println("******************************************************");
		
		for (Runnable taskWorker : runnableWorkers) {
			new Thread(taskWorker).start();
		}
		
		TimeUnit.MILLISECONDS.sleep(TTL + (1*1000) /* Extra 1 second grace */);
	
		/*
		 * Collect stats of the worker/consumer
		 */
		TaskWorker tWorker;
		workerId = 1;
		for (Runnable taskWorker : runnableWorkers) {
			tWorker = (TaskWorker) taskWorker;
			System.err.println("Worker["+ workerId +"] completed "+ tWorker.getCompletedTasksCount() +" tasks in "+ tWorker.getCompletedTasksTime() +"ms.");
			++workerId;
		}
		
		System.out.println(">>>Consumers stopping at "+ FORMAT_yyyymmdd_hhmmss.format(new Date()));
		
		System.exit(0);
	}
	
	
	/**
	 * Inner class that simulates parallel consumers
	 */
	private static class TaskWorker implements Runnable
	{
		/*
		 * @Refer TaskSenderApp.TASK_TEMPLATE = Task[#msg_counter]: Complexity[#task_complexity]
		 * 		  Pattern = \[\d+\]
		 */
		private static final String TASK_TEMPLATE_REGEX = "\\d+";
		private static final Pattern TASK_PATERN = Pattern.compile(TASK_TEMPLATE_REGEX);
		
		private int id = 1;
		private boolean roundRobinMode = true;
		private int TTL;
		
		private Channel channel = null;
		
		private int completedTasksCount = 0;
		private int completedTasksTime  = 0;
				
		public TaskWorker(int workerId, boolean roundRobinFlag, int TTL) {
			this.id = workerId;
			this.roundRobinMode = roundRobinFlag;
			this.TTL = TTL;
			
	        try {
				channel = AMQPConnectionUtility.openRabbiMQChannel(AMQPConnectionUtility.QUEUE_NAME_MULTI_TASK);
			} catch (KeyManagementException kmEx) {
				kmEx.printStackTrace();
			} catch (NoSuchAlgorithmException noAlgoEx) {
				noAlgoEx.printStackTrace();
			} catch (URISyntaxException URIEx) {
				URIEx.printStackTrace();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			} catch (TimeoutException timeoutEx) {
				timeoutEx.printStackTrace();
			}
	        
	        System.out.println("Worker "+ id +" started >>> Waiting for messages. To exit press CTRL+C");
		}
		
		public int getCompletedTasksCount() {
			return this.completedTasksCount;
		}
		
		/**
		 * 
		 * @return total time taken by worker/consumer
		 */
		public int getCompletedTasksTime() {
			return this.completedTasksTime * 1000;
		}
		
		public void run()
		{
			if (!roundRobinMode) {
				try {
					/*
					 * 
					 * Set Quality of Service
					 * i.e. Let RabbitMQ allow to send message to Consumer only if it received ACK from that Consumer
					 * 
					 */
					channel.basicQos(PREFETCH_COUNT);
				} catch (IOException qosIOEx) {
					System.err.println("Unable to set PREFETCH_COUNT=1 for Worker:"+ id);
				}
			}
			
			final Consumer consumer = new DefaultConsumer(channel)
									   {
									      @Override
									      public void handleDelivery(String consumerTag,
									    		  					 Envelope envelope,
									    		  					 AMQP.BasicProperties properties,
									    		  					 byte[] body) 
									    		  	  throws IOException
									      {
									        String taskMsg = new String(body);
									        
									        int complexity = 0;
									        try
									        {
										        int[] values = extractInfo(taskMsg);
										        if (values == null) {
										        	System.err.println("Unable to parse message for Id & Complexity");
										        	return;
										        }
										        
												int taskId = values[0];
												complexity = values[1];
												
										        doWork(taskMsg, taskId, complexity);
									        }
									        catch (InterruptedException interruptedEx) {
									        	interruptedEx.printStackTrace();
									        }    
										        
									        boolean multiple = false;
									        channel.basicAck(envelope.getDeliveryTag(), multiple);
									        
									        ++completedTasksCount;
									        completedTasksTime += complexity;
									        
									        if (getCompletedTasksTime() >= TTL) {
									        	System.err.println("TTL crossed...Stopping thread "+ id);
									        	Thread.currentThread().interrupt();
									        }
									      }
			    					   };
			
		    try {
		    	boolean autoAck = false;
				channel.basicConsume(AMQPConnectionUtility.QUEUE_NAME_MULTI_TASK, autoAck, consumer);
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
		}
		
		private void doWork(String taskMsg, int taskId, int complexity)
					 throws InterruptedException
		{
			logWork(" Worker ["+ id +"] Received task: '" + taskMsg + "'");
			
			// Simulate "complexity" of the task
			int sleepDelayInSecs = complexity * 1000;
			
			logWork(" Worker ["+ id +"] busy for "+ complexity + " seconds.");
			
		    Thread.sleep(sleepDelayInSecs);
			
			logWork(" Worker ["+ id +"] Task "+ taskId +" Done");
		}
		
		private int[] extractInfo(String taskMsg)
		{
			logWork(" [DEBUG:"+id+"]*"+ taskMsg +"*");
			
			Matcher matcher = TASK_PATERN.matcher(taskMsg);
			
			int[] retValues = new int[2];
			
			int idx = 0;
			boolean found = false;
			int matchStartIdx=0;
			int matchEndIdx=0;
			String matchedNumStr;
            while (matcher.find()) {
               matchedNumStr = matcher.group();
               matchStartIdx = matcher.start();
               matchEndIdx   = matcher.end();

                if (!matchedNumStr.isEmpty()) {
                	retValues[idx] = Integer.parseInt(matchedNumStr);
                	idx++;
                }
                
                found = true;
            }
            
            if (!found) 
            	System.err.println("Unable to find match!");
            
            return retValues;
		}
		
		private void logWork(String workText)
		{
			for(int i=0; i<id; i++)
				System.out.print("\t");
			
			System.out.print(workText);
			System.out.println();
		}
	}	// class TaskWorker

}
