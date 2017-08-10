package com.productiveAnalytics.rabbitmq.pubsub_direct;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import com.productiveAnalytics.rabbitmq.AMQPConnectionUtility;
import com.productiveAnalytics.rabbitmq.AMQPConnectionUtility.SEVERITY;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

public class EmitLogsDirect
{
	private EmitLogsDirect() {
		// do not allow instantiation
	}
	
	public static void main( String[] args )
    {
		EmitLogsDirect sender = new EmitLogsDirect();
        
        Channel channel = null;
        try {
			channel = AMQPConnectionUtility.openRabbiMQChannelForExchange(AMQPConnectionUtility.EXCHANGE_LOGS_DIRECT, BuiltinExchangeType.DIRECT);
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
	        		int severityCode = (i % 4);
	        		SEVERITY severityEnum = SEVERITY.getSeverityByCode(severityCode);
	        		String severity = severityEnum.toString();
	        		
	        		msg = "["+ severity +"]: This is log message via Direct exchange "+ AMQPConnectionUtility.FORMAT_yyyymmdd_hhmmss.format(new Date());
	        		
					sender.sendMessage(channel, msg, severity);
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
	
	public void sendMessage(Channel channel, String msg, String severity)
			 throws IOException
	{
		/*
		 * 
		 * channel.basicPublish(...) is the work horse of the Sender
		 * 
		 */
		channel.basicPublish(AMQPConnectionUtility.EXCHANGE_LOGS_DIRECT,
							 severity,
							 null,
							 msg.getBytes());
		
		System.out.println(" [x] Sent '" + msg + "'");
	}
}
