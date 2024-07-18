package com.varun.metrics.di.metricupdater;

import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.varun.metrics.service.MetricUpdaterService;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class MetricUpdaterServiceProxyImplTest {

    @Mock
    private MetricUpdaterService metricUpdaterService;

    @Mock
    private Vertx vertx;

    @Mock
    private JsonObject config;

    private MetricUpdaterServiceProxyImpl proxy;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        proxy = new MetricUpdaterServiceProxyImpl(vertx, config, metricUpdaterService);
    }

    @Test
    public void testUpdate() {
        proxy.update("");
        Mockito.verify(metricUpdaterService).updateMetric(anyString());
    }

    @Test
    public void testHandleExpiredEntries() {
        proxy.handleExpiredEntries();
        Mockito.verify(metricUpdaterService).handleExpiredEntries();
    }
}