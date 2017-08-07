package com.productiveAnalytics.rabbitmq.simple;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URISyntaxException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import com.productiveAnalytics.rabbitmq.AMQPConnectionUtility;


/**
 * Hello world!
 *
 */
public class SenderApp 
{
	private static SimpleDateFormat FORMAT_yyyymmdd_hhmmss = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS a");
	
    public static void main( String[] args )
    {
        SenderApp sender = new SenderApp();
        
        Channel channel = null;
        try {
			channel = AMQPConnectionUtility.openRabbiMQChannel(AMQPConnectionUtility.QUEUE_NAME_SIMPLE);
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
        	String msg = null;
        	try {
        		while (true)
        		{
	        		msg = "Secret message: This is my message via RabbitMQ "+ FORMAT_yyyymmdd_hhmmss.format(new Date());
					sender.sendMessage(channel, msg);
					
					try {
						int senderSleepDelay = (int) (Math.random() * Math.random() * 13333);
						Thread.sleep(senderSleepDelay);
					} catch (InterruptedException interruptedEx) {
						interruptedEx.printStackTrace();
					}
        		}
			} catch (UnsupportedEncodingException encodingEx) {
				encodingEx.printStackTrace();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
        }
        
        if (channel !=  null)
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
    
    public void sendMessage(Channel channel, String msg)
    			 throws UnsupportedEncodingException, IOException
    {
    	/*
    	 * 
    	 * channel.basicPublish(...) is the work horse of the Sender
    	 * 
    	 */
    	channel.basicPublish(AMQPConnectionUtility.DEFAULT_EXCHANGE,
    						 AMQPConnectionUtility.QUEUE_NAME_SIMPLE,
    						 null,
    						 msg.getBytes("UTF-8"));
    	
    	System.out.println(" [x] Sent '" + msg + "'");
    }
    
}
