package org.rabbitmq;

import java.io.IOException;
import java.util.Map;

import org.rabbitmq.task.MQTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;

public abstract class MQWorker extends MQEndPoint implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MQWorker.class);

    private int workerId;
    private int processed = 1;

    private Channel channel;
    private String queue;

    public MQWorker(Channel channel, String queue) {
        super(channel);
        this.channel = channel;
        this.queue = queue;
    }

    public void run() {
        try {
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, false, consumer);

            LOG.debug("Worker {}: Awaiting RCP Requests", workerId);
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                BasicProperties props = delivery.getProperties();
                BasicProperties replyProps = new BasicProperties.Builder()
                    .correlationId(props.getCorrelationId())
                    .build();

                String correlationId = props.getCorrelationId();
                long tag = delivery.getEnvelope().getDeliveryTag();

                try {
                    String message = new String(delivery.getBody());
                    LOG.debug("Worker {}: Incoming Message: {}..", workerId, message.substring(0, 50));

                    JSONRPC2Request request = JSONRPC2Request.parse(message);
                    Map<String,Object> params = request.getNamedParams();
                    NamedParamsRetriever np = new NamedParamsRetriever(params);

                    processRequest(request, np, props, tag);
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
                }
            }
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        catch (IOException ioe) {
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

    protected void initializeTask(MQTask task, MQWorker mqWorker, long tag, BasicProperties props, Object id) {
        task.setWorker(mqWorker);
        task.setTag(tag);
        task.setProps(props);
        task.setRequestId(id);
        task.setChannel(this.channel);

    }

    public abstract JSONRPC2Response processRequest(JSONRPC2Request request, NamedParamsRetriever np, BasicProperties props, long tag) throws JSONRPC2Error;

}
