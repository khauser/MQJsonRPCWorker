package org.rabbitmq;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.rabbitmq.exception.ErrorCode;
import org.rabbitmq.exception.JsonConversionException;
import org.rabbitmq.exception.MQConnectionException;
import org.rabbitmq.task.MQAsyncTask;
import org.rabbitmq.utility.JsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;

public abstract class MQAsyncWorker implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MQAsyncWorker.class);

    private int workerId;
    private int processed = 1;

    private Channel channel;
    private String queue;

    private MQAsyncTask task;

    @Autowired
    public MQAsyncWorker(Channel channel, String queue) {
        this.channel = channel;
        this.queue = queue;
    }

    public MQAsyncTask getTask() {
        return task;
    }

    public void setTask(MQAsyncTask task) {
        this.task = task;
    }

    public void run() {
        try {
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, false, consumer);

            LOG.debug("Worker {}: Awaiting RCP Requests", workerId);
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                BasicProperties props = delivery.getProperties();
                String correlationId = props.getCorrelationId();
                BasicProperties replyProps = new BasicProperties.Builder().correlationId(correlationId).build();

                long tag = delivery.getEnvelope().getDeliveryTag();

                try {
                    String message = new String(delivery.getBody());
                    LOG.debug("Worker {}: Incoming Message: {}..", workerId, message.substring(0, 50));

                    JSONRPC2Request request = JSONRPC2Request.parse(message);
                    Map<String,Object> params = request.getNamedParams();
                    NamedParamsRetriever np = new NamedParamsRetriever(params);

                    initializeRequest(request, np, props, tag);

                    String taskJsonString = JsonConverter.getObjectMapper().writeValueAsString(this.getTask());
                    sendResponse(new JSONRPC2Response(taskJsonString, correlationId), props, replyProps, tag, correlationId);

                    processRequest();
                }
                catch (JSONRPC2Error jre) {
                    LOG.error(jre.getMessage(), jre);
                    sendErrorResponse(new JSONRPC2Response(jre, correlationId), props, replyProps, tag, correlationId);
                }
                catch (JSONRPC2ParseException e) {
                    LOG.error(e.getMessage(), e);
                    sendErrorResponse(new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR, correlationId), props, replyProps, tag, correlationId);
                }
                catch (IllegalArgumentException iae) {
                    LOG.error(iae.getMessage(), iae);
                    sendErrorResponse(new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, correlationId), props, replyProps, tag, correlationId);
                } catch (JsonConversionException jce) {
                    LOG.error(jce.getMessage(), jce);
                    sendErrorResponse(new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR, correlationId), props, replyProps, tag, correlationId);
                }
            }
        }
        catch (InterruptedException ie) {
            LOG.error(ie.getMessage(), ie);
            Thread.currentThread().interrupt();
        }
        catch (IOException ioe) {
            LOG.error(ioe.getMessage(), ioe);
            Thread.currentThread().interrupt();
        }
        catch (MQConnectionException mce) {
            LOG.error(mce.getMessage(), mce);
            Thread.currentThread().interrupt();
        }
        catch (ShutdownSignalException sse) {
            Thread.currentThread().interrupt();
        }
    }

    public Channel getChannel() {
        return channel;
    }
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getWorkerId() {
        return workerId;
    }
    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public int getProcessed() {
        return processed;
    }
    public void setProcessed(int processed) {
        this.processed = processed;
    }

    protected void initializeTask(MQAsyncTask task, MQAsyncWorker mqWorker, long tag, BasicProperties props, Object id) {
        task.setWorker(mqWorker);
        task.setTag(tag);
        task.setProps(props);
        task.setRequestId(id);
        task.setChannel(this.channel);
    }

    public abstract void initializeRequest(JSONRPC2Request request, NamedParamsRetriever np, BasicProperties props
            , long tag) throws JSONRPC2Error, JsonConversionException;

    public abstract void processRequest();

    private void sendErrorResponse(JSONRPC2Response errorResponse, BasicProperties props, BasicProperties replyProps, long tag, String correlationId) throws IOException {
        byte[] responseRaw = null;
        try {
            responseRaw = errorResponse.toString().getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            LOG.error(uee.getMessage());
            errorResponse = new JSONRPC2Response(JSONRPC2Error.INVALID_REQUEST, correlationId);
            responseRaw = errorResponse.toString().getBytes();
        }
        LOG.debug("Worker {}: Sending Error Response: {}", workerId, responseRaw);

        channel.basicPublish( "", props.getReplyTo(), replyProps, responseRaw);
        channel.basicAck(tag, false);
    }

    public void sendResponse(JSONRPC2Response responseObject, BasicProperties props, BasicProperties replyProps, long tag, String correlationId) throws MQConnectionException {
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
            this.setProcessed(this.getProcessed() + 1);
        } catch (IOException e) {
            throw new MQConnectionException(ErrorCode.MQ_IO_EXCEPTION, e);
        }
    }
}

