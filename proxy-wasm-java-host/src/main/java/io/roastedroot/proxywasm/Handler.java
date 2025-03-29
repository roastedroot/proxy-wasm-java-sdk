package io.roastedroot.proxywasm;

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
