package org.rabbitmq.exception;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ServiceException extends Exception {

    private static final long serialVersionUID = -3614150197866765494L;

    private ErrorCode errorCode;
    protected Map<String, Object> params;

    public ServiceException() {
        super();
        params = new HashMap<String, Object>();
    }

    public ServiceException(ErrorCode c) {
        super(c.getMsg());
        this.errorCode = c;
        params = new HashMap<String, Object>();
    }

    public ServiceException(ErrorCode c, Throwable t) {
        super(c.getMsg(), t);
        this.errorCode = c;
        params = new HashMap<String, Object>();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode c) {
        this.errorCode = c;
    }

    /* Return value to concatenate param() calls on creation,
     * e.g. new Exception(...).param(...).param(...)
     */
    public ServiceException param(String key, Object value) {
        params.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        String result = "";

        if (errorCode != null) {
            result += errorCode.getId() + ": " + errorCode.getMsg();
        }
        if (!params.isEmpty()) {
            result += " Parameters: ";
        }
        Iterator<Entry<String, Object>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Object> pair = (Entry<String, Object>) it.next();
            result += pair.getKey() + " = " + pair.getValue() + ", ";
        }
        return result;
    }
}
