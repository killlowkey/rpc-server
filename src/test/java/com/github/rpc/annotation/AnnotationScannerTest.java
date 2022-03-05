package com.github.rpc.annotation;

import com.github.rpc.core.RpcServiceConfiguration;
import com.github.rpc.invoke.MethodContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ray
 * @date created in 2022/3/4 14:35
 */
@RunWith(JUnit4.class)
public class AnnotationScannerTest {

    String packetName = "com.github.rpc.annotation";
    Map<String, MethodContext> rpcComponents;
    Map<String, RateLimitEntry> rateLimitEntryMap;

    @Before
    public void setup() {
        RpcServiceConfiguration configuration = new RpcServiceConfiguration();
        AnnotationScanner scanner = new AnnotationScanner(packetName, configuration);
        scanner.scan();

        rpcComponents = configuration.getRpcComponents();
        rateLimitEntryMap = configuration.getRateLimitEntryMap();
    }

    @Test
    public void rpcServiceTest() {
        MethodContext context = rpcComponents.get("rpc/component/say");
        assertNotNull(context);
        assertEquals("rpc/component/say", context.getName());
    }

    @Test
    public void rateLimitTest() {
        RateLimitEntry rateLimitEntry = rateLimitEntryMap.get("rpc/component/say");
        assertEquals(50, rateLimitEntry.getLimit());
        assertEquals(TimeUnit.HOURS, rateLimitEntry.getTimeUnit());
    }

    @Test
    public void aliasTest() {
        assertNotNull(rpcComponents.get("alias say rpc"));

        MethodContext methodContext = rpcComponents.get("helloAlias");
        RateLimitEntry rateLimitEntry = rateLimitEntryMap.get("helloAlias");
        assertNotNull(methodContext);
        assertNotNull(rateLimitEntry);
        assertEquals("helloAlias", methodContext.getName());

        MethodContext methodContext1 = rpcComponents.get("RpcComponent02/helloMethodAlias");
        RateLimitEntry rateLimitEntry1 = rateLimitEntryMap.get("RpcComponent02/helloMethodAlias");
        assertNotNull(methodContext1);
        assertNotNull(rateLimitEntry1);
        assertEquals("RpcComponent02/helloMethodAlias", methodContext1.getName());

    }
}