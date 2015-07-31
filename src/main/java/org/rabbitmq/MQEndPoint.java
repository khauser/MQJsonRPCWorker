package org.rabbitmq;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.rabbitmq.exception.ErrorCode;
import org.rabbitmq.exception.MQConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class MQEndPoint {

    private static final Logger LOG = LoggerFactory.getLogger(MQEndPoint.class);

    private Channel channel;

    @Autowired
    public MQEndPoint(Channel channel) {
        this.channel = channel;
    }

    protected void sendErrorResponse(JSONRPC2Response errorResponse, BasicProperties props, BasicProperties replyProps, long tag
            , String correlationId) throws IOException {
        byte[] responseRaw = null;
        try {
            responseRaw = errorResponse.toString().getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            LOG.error(uee.getMessage());
            errorResponse = new JSONRPC2Response(JSONRPC2Error.INVALID_REQUEST, correlationId);
            responseRaw = errorResponse.toString().getBytes();
        }
        LOG.debug("Sending Error Response: {}", responseRaw);

        channel.basicPublish( "", props.getReplyTo(), replyProps, responseRaw);
        channel.basicAck(tag, false);
    }

    protected void sendResponse(JSONRPC2Response responseObject, BasicProperties props, BasicProperties replyProps, long tag, String correlationId)
            throws MQConnectionException {
        byte[] response;
        try {
            response = responseObject.toString().getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }

        try {
            LOG.debug("Sending rpc rabbitmq response to '{}'", props.getReplyTo());

            channel.basicPublish( "", props.getReplyTo(), replyProps, response);
            channel.basicAck(tag, false);
        } catch (IOException e) {
            throw new MQConnectionException(ErrorCode.MQ_IO_EXCEPTION, e);
        }
    }

}
