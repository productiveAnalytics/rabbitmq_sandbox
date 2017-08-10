package com.productiveAnalytics.rabbitmq.pubsub_fanout;

import java.io.IOException;

import java.net.URISyntaxException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import java.util.Date;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import com.productiveAnalytics.rabbitmq.AMQPConnectionUtility;

public class ReceiveLogs {
	public static void main(String[] args)
				       throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException
	{
		Channel channel = null;
        try {
			channel = AMQPConnectionUtility.openRabbiMQChannelForExchange(AMQPConnectionUtility.EXCHANGE_LOGS_FANOUT, BuiltinExchangeType.FANOUT);
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
        	 * Firstly, whenever we connect to Rabbit we need a fresh, empty queue.
        	 *    To do this we could create a queue with a random name, or, even better - let the server choose a random queue name for us.
			 * 
			 * Secondly, once we disconnect the consumer the queue should be automatically deleted.
			 * 
			 * So call channel.queueDeclare().getQueue() to get non-durable, exclusive and auto-delete Queue with generated name
        	 */
        	final String generated_queueName = channel.queueDeclare().getQueue();

        	final String routingKey = "";
        	channel.queueBind(generated_queueName, AMQPConnectionUtility.EXCHANGE_LOGS_FANOUT, routingKey);	// For fanout exchange, routing_key is ignored
        	
        	System.out.println("Bound generated Queue:"+ generated_queueName +" to Exchange: "+ AMQPConnectionUtility.EXCHANGE_LOGS_FANOUT);
        	
        	Consumer qConsumer = new DefaultConsumer(channel) {
					        		
        							  @Override
					        		  public void handleDelivery(String consumerTag, 
					        				  					 Envelope envelope,
					        		                             AMQP.BasicProperties properties,
					        		                             byte[] body)
					        		              throws IOException
        							  {
        								  String receivedMsg = new String(body);
        					        	  System.out.println(" [x] Received @ : "+ AMQPConnectionUtility.FORMAT_yyyymmdd_hhmmss.format(new Date()) + ": " + receivedMsg );
					        		  }
        						 };
        	
        	final boolean autoACK = true;
       	    channel.basicConsume(generated_queueName, autoACK, qConsumer);
        }
	}
}
