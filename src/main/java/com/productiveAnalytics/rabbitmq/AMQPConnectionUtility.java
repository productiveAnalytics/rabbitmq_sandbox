package com.productiveAnalytics.rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class AMQPConnectionUtility 
{
	/**
	 * amqp://ecmforgi:IhFm6sHjpKsRQNKhn1eXUfwgN1swyB1C@wombat.rmq.cloudamqp.com/ecmforgi
	 */
	public static String CLOUDAMQP_URL = "amqp://wombat.rmq.cloudamqp.com/ecmforgi";
	
	public static String CLOUDAMQP_USERNAME = "ecmforgi";
	public static String CLOUDAMQP_PASSWORD = "IhFm6sHjpKsRQNKhn1eXUfwgN1swyB1C";
	
	public static String DEFAULT_EXCHANGE = "";
	
	public static String QUEUE_NAME_SIMPLE 		= "simple_queue";
	public static String QUEUE_NAME_MULTI_TASK 	= "multi_task_queue";
	
	public static Channel openRabbiMQChannel(String queueName)
				    		throws KeyManagementException, 
				    			   NoSuchAlgorithmException,
				    			   URISyntaxException,
				    			   IOException,
				    			   TimeoutException
    {
    	ConnectionFactory conFactory = new ConnectionFactory();
    	
    	conFactory.setUri(CLOUDAMQP_URL);
    	conFactory.setUsername(CLOUDAMQP_USERNAME);
    	conFactory.setPassword(CLOUDAMQP_PASSWORD);
    	conFactory.setConnectionTimeout(30000);
    	
    	Connection conxn = conFactory.newConnection();
    	Channel channel = conxn.createChannel();
    	
    	boolean durable    = true;  //durable - RabbitMQ will never lose the queue if a crash occurs
    	boolean exclusive  = false; //exclusive - if queue only will be used by one connection
    	boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes
    	
    	channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
    	
    	System.out.println("Opened RabbitMQ Channel & Connection >>>");
    	return channel;
    }
	
    public static void closeRabbitMQChannel(Channel channel)
			 			throws IOException,
			 				   TimeoutException
    {
		if (channel !=  null) {
			channel.close();
			channel.getConnection().close();
			System.out.print(">>> Closed RabbitMQ Channel & Connection");
		}
    }
}