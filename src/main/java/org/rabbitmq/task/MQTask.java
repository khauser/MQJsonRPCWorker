package org.rabbitmq.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rabbitmq.MQWorker;
import org.rabbitmq.exception.ErrorCode;
import org.rabbitmq.exception.JsonConversionException;
import org.rabbitmq.exception.MQConnectionException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;

public abstract class MQTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MQTask.class);

    private long tag;
    private Channel channel;
    private BasicProperties props;
    private MQWorker worker;
    private Object requestId;

    public MQTask() {}

    public long getTag() {
        return tag;
    }

    public void setTag(long tag) {
        this.tag = tag;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public BasicProperties getProps() {
        return props;
    }

    public void setProps(BasicProperties props) {
        this.props = props;
    }

    public MQWorker getWorker() {
        return worker;
    }

    public void setWorker(MQWorker worker) {
        this.worker = worker;
    }

    public Object getRequestId() {
        return requestId;
    }

    public void setRequestId(Object requestId) {
        this.requestId = requestId;
    }

    public void run() {
        LOG.debug("Starting rpc rabbitmq task");
        try {
            Object responseObject = this.executeTask();
            if (channel.isOpen()) {
                sendResponse(responseObject);
            }
            LOG.debug("Ending rpc rabbitmq task");
        }
        catch (JsonConversionException jce) {
            LOG.error(jce.getMessage());
            try {
                sendResponse(internalError(this.getRequestId(), jce.getMessage()).toString().getBytes());
            } catch (MQConnectionException e) {
               LOG.error("Error while sending  RPC response.", e);
            }
        }
        catch (Exception e) {
            LOG.error(e.getMessage());
            try {
                sendResponse(internalError(this.getRequestId(), e.getMessage(), backTrace(e)));
            } catch (MQConnectionException e1) {
                LOG.error("Error while sending  RPC response.", e1);
            }
        }
    }

    public void sendResponse(Object responseObject) throws MQConnectionException {
        byte[] response;
        if (responseObject == null) {
            response = null;
        }
        else if (responseObject instanceof byte[]) {
            response = (byte[])responseObject;
        }
        else if (responseObject instanceof JSONRPC2Response) {
            try {
                response = responseObject.toString().getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException uee) {
                throw new RuntimeException(uee);
            }
        }
        else {
            // TODO Send also here an ErrorResponse
            response = null;
        }

        BasicProperties replyProps = new BasicProperties.Builder()
            .correlationId(props.getCorrelationId())
            .build();
        try {
            LOG.debug("Sending rpc rabbitmq response to '{}'", this.props.getReplyTo());

            channel.basicPublish( "", this.props.getReplyTo(), replyProps, response);
            channel.basicAck(tag, false);
            worker.setProcessed(worker.getProcessed() + 1);
        } catch (IOException e) {
            throw new MQConnectionException(ErrorCode.MQ_IO_EXCEPTION, e);
        }
    }

    public abstract void setUpTask(NamedParamsRetriever np) throws JSONRPC2Error;

    public abstract Object executeTask() throws Exception;

    public JSONRPC2Response internalError(Object request_id, String message) {
        return internalError(request_id, message, null);
    }
    public JSONRPC2Response internalError(Object request_id, String message, String backtrace) {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("exception", message);
        if (backtrace != null) {
            data.put("backtrace", backtrace);
        }
        JSONRPC2Error err = new JSONRPC2Error(-32603, "Internal Server Error", data);
        return new JSONRPC2Response(err, request_id);
    }
    public String backTrace(Throwable e) {
        StackTraceElement[] stack = e.getStackTrace();
        String trace = "";
        for (int i=0; i<stack.length; ++i) {
            trace += stack[i].toString() + "\n";
        }
        return trace;
    }
}
