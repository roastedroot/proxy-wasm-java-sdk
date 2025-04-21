package io.roastedroot.proxywasm.internal;

import io.roastedroot.proxywasm.LogHandler;
import io.roastedroot.proxywasm.MetricsHandler;
import io.roastedroot.proxywasm.SharedDataHandler;
import io.roastedroot.proxywasm.SharedQueueHandler;

public interface Handler
        extends ContextHandler,
                LogHandler,
                PluginHandler,
                PropertiesHandler,
                HttpContextHandler,
                HttpCallHandler,
                SharedDataHandler,
                SharedQueueHandler,
                FFIHandler,
                GRPCContextHandler,
                MetricsHandler,
                CustomHandler {

    /**
     * The default handler.  It holds no state.
     */
    Handler DEFAULT = new Handler() {};
}
