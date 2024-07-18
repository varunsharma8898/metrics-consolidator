package com.varun.metrics.di.metricupdater;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;

@VertxGen
@ProxyGen
public interface MetricUpdaterServiceProxy {

    public static final String METRIC_UPDATER_SERVICE_ADDRESS = "vertx-metric-updater-service";

    void update(String rawData);

    void handleExpiredEntries();

}
