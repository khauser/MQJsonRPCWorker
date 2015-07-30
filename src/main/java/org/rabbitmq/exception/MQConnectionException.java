package org.rabbitmq.exception;

public class MQConnectionException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public MQConnectionException(ErrorCode c) {
        super(c);
    }

    public MQConnectionException(ErrorCode c, Throwable t) {
        super(c, t);
    }

    /* Return value to concatenate param() calls on creation,
     * e.g. new Exception(...).param(...).param(...)
     */
    public MQConnectionException param(String key, Object value) {
        params.put(key, value);
        return this;
    }

}
