package com.productiveAnalytics.rabbitmq.mutlitaskQueue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import java.util.concurrent.TimeoutException;

import com.productiveAnalytics.rabbitmq.AMQPConnectionUtility;
import com.productiveAnalytics.rabbitmq.simple.SenderApp;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

public class TaskSenderApp extends SenderApp
{
	private static String TASK_TEMPLATE = "Task[#msg_counter]: Complexity[#task_complexity]";
		
	public static void main( String[] args )
    {
		TaskSenderApp taskSender = new TaskSenderApp();
        
        Channel channel = null;
        try {
			channel = AMQPConnectionUtility.openRabbiMQChannelForQueue(AMQPConnectionUtility.QUEUE_NAME_MULTI_TASK);
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
        
        if (channel !=  null)
        {
        	String taskMsg = null;
        	try {
        		for (int msgCounter=0; msgCounter < 10000; ++msgCounter)
        		{
        			/*
        			 * Complexity of task "simulates" number of seconds the task would take to execute. 
        			 */
        			int taskComplexity = randomWithRange(1,10);
        			
	        		taskMsg = TASK_TEMPLATE.replace("#msg_counter", String.valueOf(msgCounter))
	        						  	   .replace("#task_complexity", String.valueOf(taskComplexity));

					taskSender.sendMessage(channel, taskMsg);
        		}
			} catch (UnsupportedEncodingException encodingEx) {
				encodingEx.printStackTrace();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
        }
        
        if (channel != null)
        {
        	try {
        		AMQPConnectionUtility.closeRabbitMQChannel(channel);
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			} catch (TimeoutException timeoutEx) {
				timeoutEx.printStackTrace();
			}
        }
    }
	
	@Override
	public void sendMessage(Channel channel, String msg)
			 throws IOException
	{
		/*
		 * 
		 * channel.basicPublish(...) is the work horse of the Sender
		 * 
		 */
		channel.basicPublish(AMQPConnectionUtility.DEFAULT_EXCHANGE,
							 AMQPConnectionUtility.QUEUE_NAME_MULTI_TASK,
							 MessageProperties.PERSISTENT_TEXT_PLAIN,	// text/plain & deliveryMode=Persistent
							 msg.getBytes());
		
		System.out.println(" [x] Sent '" + msg + "'");
	}
	
	/**
	 * Generate random number between min & max range
	 * 
	 * @param min
	 * @param max
	 * 
	 * @return random number within range
	 */
	private static int randomWithRange(int min, int max)
	{
	   int range = Math.abs(max - min) + 1;
	   return (int) (Math.random() * range) + Math.min(min, max);
	}
}
