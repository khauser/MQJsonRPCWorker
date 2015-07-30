package org.rabbitmq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rabbitmq.config.ServiceConfiguration;
import org.rabbitmq.exception.JsonConversionException;
import org.rabbitmq.utility.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:integration.properties")
@WebAppConfiguration
@SpringApplicationConfiguration(classes = RpcServerApplication.class)
public class TestMQTest {

    private static final String EXPECTED_TEST_METHOD = "testMethod";
    private static final String EXPECTED_GUID = UUID.randomUUID().toString();
    private static final String EXPECTED_TYPE = "org.rabbitmq.TestAsyncTask";

    @Autowired
    private ServiceConfiguration imageServiceConfig;

    @Autowired
    private TestMQClient rpcClient;

    @Before
    public void setUp() {
        //URL baseUrl = new URL(serviceConfig.getServiceBaseURL().toString());
    }

    @Test
    public void testRPC() throws JSONRPC2ParseException, JsonConversionException, JsonParseException, JsonMappingException, IOException {
        String method = EXPECTED_TEST_METHOD;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("guid", EXPECTED_GUID);

        // guid could also be used as correlationId/requestId
        JSONRPC2Request reqOut = new JSONRPC2Request(method, params, EXPECTED_GUID);
        byte[] responseBytes = (byte[])rpcClient.executeRequest(reqOut);
        JSONRPC2Response response = JSONRPC2Response.parse(new String(responseBytes));

        assertTrue(response.indicatesSuccess());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());

        JsonNode taskJson = JsonConverter.readTree(response.getResult().toString());
        assertFalse(taskJson.get("id").textValue().isEmpty());
        assertEquals(EXPECTED_GUID, taskJson.get("data").get("guid").textValue());
        assertEquals(EXPECTED_TYPE, taskJson.get("type").textValue());
    }

}
