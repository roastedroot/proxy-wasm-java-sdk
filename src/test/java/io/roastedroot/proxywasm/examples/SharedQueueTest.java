package io.roastedroot.proxywasm.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.QueueName;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.WasmException;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Test case to verify src/test/go-examples/shared_queue example.
 */
public class SharedQueueTest {
    private static final WasmModule receiverModule =
            Parser.parse(Path.of("./src/test/go-examples/shared_queue/receiver/main.wasm"));
    private static final WasmModule senderModule =
            Parser.parse(Path.of("./src/test/go-examples/shared_queue/sender/main.wasm"));

    ArrayList<Closeable> closeList = new ArrayList<>();

    @AfterEach
    void tearDown() {
        Collections.reverse(closeList);
        for (Closeable closeable : closeList) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    <T extends Closeable> T deferClose(T x) {
        closeList.add(x);
        return x;
    }

    @Test
    public void testOnPluginStart() throws StartException, WasmException {
        var sharedData = new MockSharedHandler();

        var receiverVmId = "receiver";

        // Create and configure the http_request_headers receiver instance
        var receiverHandler1 = new MockHandler(sharedData);
        var receiverHost1 =
                deferClose(
                        ProxyWasm.builder()
                                .withPluginHandler(receiverHandler1)
                                .withProperties(Map.of("vm_id", receiverVmId))
                                .withPluginConfig("http_request_headers")
                                .build(receiverModule));

        var requestHeadersQueueId =
                sharedData.resolveSharedQueue(new QueueName(receiverVmId, "http_request_headers"));
        receiverHandler1.assertLogsContain(
                String.format(
                        "queue \"%s\" registered as queueID=%d by contextID=%d",
                        "http_request_headers", requestHeadersQueueId, receiverHost1.contextId()));
        var requestHeadersQueue = sharedData.getSharedQueue(requestHeadersQueueId);
        assertNotNull(requestHeadersQueue);

        // Create and configure the http_response_headers receiver instance
        var receiverHandler2 = new MockHandler(sharedData);
        var receiverHost2 =
                deferClose(
                        ProxyWasm.builder()
                                .withPluginHandler(receiverHandler2)
                                .withProperties(Map.of("vm_id", receiverVmId))
                                .withPluginConfig("http_response_headers")
                                .build(receiverModule));

        var responseHeadersQueueId =
                sharedData.resolveSharedQueue(new QueueName(receiverVmId, "http_response_headers"));
        receiverHandler2.assertLogsContain(
                String.format(
                        "queue \"%s\" registered as queueID=%d by contextID=%d",
                        "http_response_headers",
                        responseHeadersQueueId,
                        receiverHost2.contextId()));
        var responseHeadersQueue = sharedData.getSharedQueue(responseHeadersQueueId);
        assertNotNull(responseHeadersQueue);

        // Create and configure the sender instance
        var senderHandler = new MockHandler(sharedData);
        var senderVmId = "sender";
        var senderHost =
                deferClose(
                        ProxyWasm.builder()
                                .withPluginHandler(senderHandler)
                                .withProperties(Map.of("vm_id", senderVmId))
                                .withPluginConfig("http")
                                .build(senderModule));
        senderHandler.assertLogsContain(
                String.format("contextID=%d is configured for %s", senderHost.contextId(), "http"));

        var senderContext = deferClose(senderHost.createHttpContext(senderHandler));

        // queue is empty
        assertEquals(0, requestHeadersQueue.data.size());

        senderHandler.setHttpRequestHeaders(Map.of("hello", "world"));
        Action action = senderContext.callOnRequestHeaders(false);
        assertEquals(Action.CONTINUE, action);
        String queuedMessage = "{\"key\": \"hello\",\"value\": \"world\"}";
        senderHandler.assertLogsContain(String.format("enqueued data: %s", queuedMessage));

        // queue now has 1 item
        assertEquals(1, requestHeadersQueue.data.size());

        // let the receiver know that the queue is ready
        receiverHost1.sendOnQueueReady(requestHeadersQueueId);

        receiverHandler1.assertLogsContain(
                String.format(
                        "(contextID=%d) dequeued data from %s(queueID=%d): %s",
                        receiverHost1.contextId(),
                        "http_request_headers",
                        requestHeadersQueueId,
                        queuedMessage));
    }
}
