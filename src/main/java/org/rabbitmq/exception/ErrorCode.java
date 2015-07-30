package org.rabbitmq.exception;

public enum ErrorCode {

    CONSTRAINT_VIOLATION_EXCEPTION(100, "Violation of database constraints in operation. "
            + "Maybe an entity with the same identifier is already persisted."),
    PERSISTENCE_EXCEPTION(101, "A general persistence exception occured."),
    RUNTIME_EXCEPTION_DAO(102, "A Runtime exception occured while performing a database operation."),

    PARAMETER_NOT_FOUND(900, "One of the provided parameters could not be found."),
    ILLEGAL_REQUEST(901, "The requested operation is not allowed."),
    ILLEGAL_NUMBER_PARAMETER(904, "The specified numeric parameter is out of range."),
    EMPTY_REQUEST_BODY(907, "Request body is empty."),
    EMPTY_FILE(908, "Upload of file failed. Reveived an empty file."),
    FILE_EXTENSION_CHANGE(909, "The extension of a file must not be empty and cannot be changed."),
    FOLDER_NOT_FOUND(910, "The requested folder could not be found."),
    TARGET_FOLDER_NOT_FOUND(911, "The requested target folder could not be found."),
    ROOTFOLDERNAME_NOT_ALLOWED(912, "The requested root folder name is not allowed."),
    IMAGE_NOT_FOUND(915, "The requested image could not be found."),
    TASK_NOT_FOUND(916, "The requested task could not be found."),

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
