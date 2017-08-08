package com.productiveAnalytics.rabbitmq.simple;

import java.io.IOException;

import java.net.URISyntaxException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
//import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import com.productiveAnalytics.rabbitmq.AMQPConnectionUtility;

public class ReceiverApp {
	public static void main(String[] args)
				       throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException
	{
		Channel channel = null;
        try {
			channel = AMQPConnectionUtility.openRabbiMQChannelForQueue(AMQPConnectionUtility.QUEUE_NAME_SIMPLE);
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
        	/*
        	 * QueueingConsumer has been Deprecated, so using DefaultConsumer
        	 */
//        	QueueingConsumer qConsumer = new QueueingConsumer(channel);
        	
        	Consumer qConsumer = new DefaultConsumer(channel) {
					        		
        							  @Override
					        		  public void handleDelivery(String consumerTag, 
					        				  					 Envelope envelope,
					        		                             AMQP.BasicProperties properties,
					        		                             byte[] body)
					        		              throws IOException
        							  {
        								  String receivedMsg = new String(body, "UTF-8");
        					        	  System.out.println(" [x] Received @ : "+ AMQPConnectionUtility.FORMAT_yyyymmdd_hhmmss.format(new Date()) + ": " + receivedMsg );
        					        	  
        					        	  try {
        					        		  int receiverSleepDelay = (int) (Math.random() * Math.random() * 13333);
        					        		  Thread.sleep(receiverSleepDelay);
        					        	  } catch (InterruptedException interruptedEx) {
        									  interruptedEx.printStackTrace();
        								  }
					        		  }
        						 };
        	
        	
        	final boolean autoACK = true;
       	
//        	while (true)
//        	{
        		/*
        		 * 
        		 * channel.basicConsume(...) is the work horse of the Receiver
        		 * 
        		 */
        		channel.basicConsume(AMQPConnectionUtility.QUEUE_NAME_SIMPLE, autoACK, qConsumer);
        		
//        		QueueingConsumer.Delivery delivery = qConsumer.nextDelivery();
//        		receivedMsg = new String(delivery.getBody());
//        		System.out.println(" [x] Received @ : "+ AMQPConnectionUtility.FORMAT_yyyymmdd_hhmmss.format(new Date()) + ": " + receivedMsg );
        		
//        		try {
//        			int receiverSleepDelay = (int) (Math.random() * Math.random() * 13333);
//					Thread.sleep(receiverSleepDelay);
//				} catch (InterruptedException interruptedEx) {
//					interruptedEx.printStackTrace();
//				}
//        	}
        }
	}
}
