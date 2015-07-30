package org.rabbitmq.exception;

public enum ErrorCode {

    OBJECT_TO_JSON_EXCEPTION(210,"Error converting object of to JSON string."),
    JSON_PARSE_EXCEPTION(211, "Cannot parse input string to JSON."),
    JSON_MAP_EXCEPTION(212,"Cannot map input string to object."),
    JSON_IO_EXCEPTION(213,"IOException while creating object from JSON string."),

    MQTASK_TO_JSON_DATA_EXCEPTION(230,"Error converting mqtask data object to JSON string."),

    MQ_IO_EXCEPTION(301, "IOException while handling RabbitMQ connection."),
    RPC_PARAMETER_MISSING(302, "The requested RPC parameter is missing."),
    RPC_PARAMETER_CONVERSION_ERROR(303, "Error converting RPC parameter to object."),
    RPC_METHOD_NOT_FOUND(303, "The requested RPC method could not be found.");

    private final int id;
    private final String msg;


    ErrorCode(int id, String msg) {
        this.id = id;
        this.msg = msg;
    }


    public int getId() {
        return this.id;
    }

    public String getMsg() {
        return this.msg;
    }
}
