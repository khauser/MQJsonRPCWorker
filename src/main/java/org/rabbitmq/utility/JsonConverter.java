package org.rabbitmq.utility;

import java.io.IOException;

import org.rabbitmq.exception.ErrorCode;
import org.rabbitmq.exception.JsonConversionException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    public static String toJson(Object jaxbElement) throws JsonConversionException {
        String json = null;
        try {
            json = MAPPER.writeValueAsString(jaxbElement);
        } catch (JsonProcessingException e) {
            throw new JsonConversionException(ErrorCode.OBJECT_TO_JSON_EXCEPTION, e).param("object", jaxbElement);
        }
        return json;
    }

    public static <T> Object toObject(String json, Class<T> clazz) throws JsonConversionException {
        Object obj = null;
        try {
            obj = MAPPER.readValue(json, clazz);
        } catch (JsonParseException jpe) {
            throw new JsonConversionException(ErrorCode.JSON_PARSE_EXCEPTION, jpe).param("json", json).param("class", clazz.getName());
        } catch (JsonMappingException jme) {
            throw new JsonConversionException(ErrorCode.JSON_MAP_EXCEPTION, jme).param("json", json).param("class", clazz.getName());
        } catch (IOException ioe) {
            throw new JsonConversionException(ErrorCode.JSON_IO_EXCEPTION, ioe).param("json", json).param("class", clazz.getName());
        }
        return obj;
    }

    public static JsonNode readTree(String json) throws JsonConversionException {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException jpe) {
            throw new JsonConversionException(ErrorCode.JSON_PARSE_EXCEPTION, jpe).param("json", json);
        } catch (IOException ioe) {
            throw new JsonConversionException(ErrorCode.JSON_IO_EXCEPTION, ioe).param("json", json);
        }   
    }
}
