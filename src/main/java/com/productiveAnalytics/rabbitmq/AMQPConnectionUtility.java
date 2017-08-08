package com.productiveAnalytics.rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.BuiltinExchangeType;
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
	public static String EXCHANGE_LOGS_FANOUT = "excng_logs";
	
	public static String QUEUE_NAME_BLANK 		= ""; // While using Exchange, no need of Queue
	
	public static String QUEUE_NAME_SIMPLE 		= "simple_queue";
	public static String QUEUE_NAME_MULTI_TASK 	= "multi_task_queue";
	
	public static SimpleDateFormat FORMAT_yyyymmdd_hhmmss = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS a");
	
	private static Connection getRabbitMQConnection()
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
    	
    	return conFactory.newConnection();
	}
	
	/**
	 * Open Channel w/ provided Queue name
	 * @param queueName
	 * @return Channel
	 * 
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public static Channel openRabbiMQChannelForQueue(String queueName)
				    		throws KeyManagementException, 
				    			   NoSuchAlgorithmException,
				    			   URISyntaxException,
				    			   IOException,
				    			   TimeoutException
    {
		Connection conxn = getRabbitMQConnection();
		Channel channel = conxn.createChannel();
    	
    	boolean durable    = true;  //durable - RabbitMQ will never lose the queue if a crash occurs
    	boolean exclusive  = false; //exclusive - if queue only will be used by one connection
    	boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes
    	
    	channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
    	
    	System.out.println("Opened RabbitMQ Channel & Connection, for Queue:"+ queueName +">>>");
    	return channel;
    }
	
	/**
	 * Open Channel w/ provided Exchange name
	 * 
	 * @param exchangeName
	 * @param exchangeType
	 * @return Channel
	 * 
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public static Channel openRabbiMQChannelForExchange(String exchangeName, BuiltinExchangeType exchangeType)
				    		throws KeyManagementException, 
				    			   NoSuchAlgorithmException,
				    			   URISyntaxException,
				    			   IOException,
				    			   TimeoutException
	{
		Connection conxn = getRabbitMQConnection();
		Channel channel = conxn.createChannel();
		
		boolean durable    = true;  //durable - RabbitMQ will never lose the Exchange if a crash occurs
    	boolean autoDelete = false; //autodelete - Exchange is deleted when last consumer unsubscribes
    	boolean internal   = false; //internal - if Exchange is for internal purpose
		
		channel.exchangeDeclare(exchangeName,
								exchangeType,
								durable,
								autoDelete,
								internal,
								null);
		
		System.out.println("Opened RabbitMQ Channel & Connection, for Exchange:"+ exchangeName +">>>");
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