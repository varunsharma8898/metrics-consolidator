package com.varun.metrics.di.metricupdater;

import com.varun.metrics.service.MetricUpdaterService;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class MetricUpdaterServiceProxyImpl implements MetricUpdaterServiceProxy {

    private Vertx vertx;

    private JsonObject config;

    MetricUpdaterService metricUpdaterService;

    public MetricUpdaterServiceProxyImpl(Vertx vertx, JsonObject config, MetricUpdaterService metricUpdaterService) {
        this.vertx = vertx;
        this.config = config;
        this.metricUpdaterService = metricUpdaterService;
    }

    public static MetricUpdaterServiceProxyImpl createProxy(Vertx vertx, JsonObject config, MetricUpdaterService metricUpdaterService) {
        return new MetricUpdaterServiceProxyImpl(vertx, config, metricUpdaterService);
    }

    @Override
    public void update(String rawData) {
        metricUpdaterService.updateMetric(rawData);
    }

    @Override
    public void handleExpiredEntries() {
        metricUpdaterService.handleExpiredEntries();
    }
}
