package org.rabbitmq.exception;

public class JsonConversionException extends ServiceException {

    private static final long serialVersionUID = -4027037843432028360L;

    public JsonConversionException(ErrorCode c) {
        super(c);
    }

    public JsonConversionException(ErrorCode c, Throwable t) {
        super(c, t);
    }

    /* Return value to concatenate param() calls on creation,
     * e.g. new Exception(...).param(...).param(...)
     */
    public JsonConversionException param(String key, Object value) {
        params.put(key, value);
        return this;
    }

    }
