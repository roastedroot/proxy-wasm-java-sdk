package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.MetricType;

public class Metric {

    final int id;
    final MetricType type;
    final String name;
    long value;

    public Metric(int id, MetricType type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    public int id() {
        return id;
    }

    public MetricType type() {
        return type;
    }

    public String name() {
        return name;
    }

    public long value() {
        return value;
    }
}
