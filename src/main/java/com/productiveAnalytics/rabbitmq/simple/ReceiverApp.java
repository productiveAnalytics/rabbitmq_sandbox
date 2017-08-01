package com.productiveAnalytics.rabbitmq.simple;

import java.io.IOException;

import java.net.URISyntaxException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import com.productiveAnalytics.rabbitmq.AMQPCommons;

public class ReceiverApp {
	private static SimpleDateFormat FORMAT_yyyymmdd_hhmmss = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS a");
	
	public static void main(String[] args)
				       throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException
	{
		Channel channel = null;
        try {
			channel = AMQPCommons.openRabbiMQChannel(AMQPCommons.QUEUE_NAME_SIMPLE);
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
        	QueueingConsumer qConsumer = new QueueingConsumer(channel);
        	boolean autoACK = true;
        	String receivedMsg = null;
        	
        	while (true)
        	{
        		channel.basicConsume(AMQPCommons.QUEUE_NAME_SIMPLE, autoACK, qConsumer);
        		
        		QueueingConsumer.Delivery delivery = qConsumer.nextDelivery();
        		receivedMsg = new String(delivery.getBody());
        		System.out.println(" [x] Received @ : "+ FORMAT_yyyymmdd_hhmmss.format(new Date()) + ": " + receivedMsg );
        		
        		try {
        			int receiverSleepDelay = (int) (Math.random() * Math.random() * 13333);
					Thread.sleep(receiverSleepDelay);
				} catch (InterruptedException interruptedEx) {
					interruptedEx.printStackTrace();
				}
        	}
        }
	}
}
