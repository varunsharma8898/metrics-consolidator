package com.varun.metrics.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.varun.metrics.dal.MetricsDAL;
import com.varun.metrics.model.VideoPlayerMetrics;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class MetricUpdaterServiceTest {

    @Mock
    private MetricsDAL metricsDal;

    @Mock
    private Vertx vertx;

    @Mock
    private JsonObject config;

    private MetricUpdaterService metricUpdaterService;
    private VideoPlayerMetrics metrics;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        metricUpdaterService = new MetricUpdaterService(vertx, config, metricsDal);
        metrics = VideoPlayerMetrics.builder().build();
    }

    @Test
    public void testUpdateMetric_bitrateOnly() {
        Mockito.when(metricsDal.getMetricByTimestampAndMachineId(123L, "test-123")).thenReturn(Single.just(metrics));
        metricUpdaterService.updateMetric("{\"video_player\": \"test-123\", \"bitrate\": 4500, \"utc_minute\": 123}");
        Mockito.verify(metricsDal).getMetricByTimestampAndMachineId(123L, "test-123");
        Mockito.verify(metricsDal).writeMetricForTimestamp(anyLong(), any(VideoPlayerMetrics.class));
        Mockito.verifyNoMoreInteractions(metricsDal);
    }

    @Test
    public void testUpdateMetric_framerateOnly() {
        Mockito.when(metricsDal.getMetricByTimestampAndMachineId(123L, "test-123")).thenReturn(Single.just(metrics));
        metricUpdaterService.updateMetric("{\"video_player\": \"test-123\", \"framerate\": 4500, \"utc_minute\": 123}");
        Mockito.verify(metricsDal).getMetricByTimestampAndMachineId(123L, "test-123");
        Mockito.verify(metricsDal).writeMetricForTimestamp(anyLong(), any(VideoPlayerMetrics.class));
        Mockito.verifyNoMoreInteractions(metricsDal);
    }

    @Test
    public void testUpdateMetric_bothMetrics() {
        VideoPlayerMetrics metrics = VideoPlayerMetrics.builder().machineId("test-123").bitrate(4500).timestamp(123L).build();
        Mockito.when(metricsDal.getMetricByTimestampAndMachineId(123L, "test-123")).thenReturn(Single.just(metrics));
        metricUpdaterService.updateMetric("{\"video_player\": \"test-123\", \"framerate\": 45, \"utc_minute\": 123}");
        Mockito.verify(metricsDal).getMetricByTimestampAndMachineId(123L, "test-123");
        Mockito.verify(metricsDal).writeMetricForTimestamp(anyLong(), any(VideoPlayerMetrics.class));
        Mockito.verify(metricsDal).removeMetricForTimestamp(anyLong(), any(VideoPlayerMetrics.class));
    }

    @Test
    public void testHandleExpiredEntries() {
        metricUpdaterService.handleExpiredEntries();
        Mockito.verify(metricsDal).handleExpiredEntries();
        Mockito.verifyNoMoreInteractions(metricsDal);
    }
}