package com.productiveAnalytics.rabbitmq.pubsub_simple;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import com.productiveAnalytics.rabbitmq.AMQPConnectionUtility;
import com.productiveAnalytics.rabbitmq.simple.SenderApp;

public class EmitLogs extends SenderApp
{
	public static void main( String[] args )
    {
		EmitLogs sender = new EmitLogs();
        
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
        	String msg = null;
        	try {
        		for (int i=0; i< 100; i++)
        		{
	        		msg = "This is log message via fanout exchange "+ AMQPConnectionUtility.FORMAT_yyyymmdd_hhmmss.format(new Date());
					sender.sendMessage(channel, msg);
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
	
	@Override
	public void sendMessage(Channel channel, String msg)
			 throws IOException
	{
		/*
		 * 
		 * channel.basicPublish(...) is the work horse of the Sender
		 * 
		 */
		channel.basicPublish(AMQPConnectionUtility.EXCHANGE_LOGS_FANOUT,
							 AMQPConnectionUtility.QUEUE_NAME_BLANK,		// Queue: ""
							 null,
							 msg.getBytes());
		
		System.out.println(" [x] Sent '" + msg + "'");
	}
}
