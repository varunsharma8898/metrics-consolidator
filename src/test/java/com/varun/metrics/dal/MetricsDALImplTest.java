package com.varun.metrics.dal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.cache.Cache;
import com.varun.metrics.model.VideoPlayerMetrics;

import io.reactivex.Single;

public class MetricsDALImplTest {

    private MetricsDAL metricsDAL;

    private VideoPlayerMetrics metrics;

    private static final Long TEST_LONG_1 = 123L;

    private static final Long TEST_LONG_2 = 456L;

    private static final String TEST_STRING = "test-123";
    private static final String TEST_STRING_2 = "test-456";

    private Cache<Long, Map<String, VideoPlayerMetrics>> cache;

    private ConcurrentHashMap<Long, Long> expirationMap;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {

        metricsDAL = new MetricsDALImpl();
        metrics = VideoPlayerMetrics.builder()
                .machineId(TEST_STRING)
                .bitrate(TEST_LONG_1)
                .framerate(TEST_LONG_2)
                .timestamp(TEST_LONG_1)
                .retries(0)
                .build();

        Field cacheField = MetricsDALImpl.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        cache = (Cache<Long, Map<String, VideoPlayerMetrics>>) cacheField.get(metricsDAL);

        Field expirationMapField = MetricsDALImpl.class.getDeclaredField("expirationMap");
        expirationMapField.setAccessible(true);
        expirationMap = (ConcurrentHashMap<Long, Long>) expirationMapField.get(metricsDAL);

    }

    @Test
    public void testWrite() {
        metricsDAL.writeMetricForTimestamp(TEST_LONG_1, metrics);

        Map<String, VideoPlayerMetrics> testMap = cache.getIfPresent(TEST_LONG_1);
        Assertions.assertNotNull(testMap);
        Assertions.assertEquals(metrics, testMap.get(TEST_STRING));
        Assertions.assertNotNull(expirationMap.get(TEST_LONG_1));
    }

    @Test
    public void testGetMetric() {
        cache.put(TEST_LONG_1, Map.of(TEST_STRING, metrics));
        Single<VideoPlayerMetrics> test = metricsDAL.getMetricByTimestampAndMachineId(TEST_LONG_1, TEST_STRING);
        test.subscribe(val -> {
            Assertions.assertNotNull(val);
            Assertions.assertEquals(metrics, val);
        });
    }

    @Test
    public void testGetMetric_whenNotPresent() {
        Single<VideoPlayerMetrics> test = metricsDAL.getMetricByTimestampAndMachineId(TEST_LONG_2, TEST_STRING);
        test.subscribe(val -> {
            Assertions.assertNotNull(val);
            Assertions.assertEquals(TEST_LONG_2, val.getTimestamp());
            Assertions.assertEquals(TEST_STRING, val.getMachineId());
        });
    }


    @Test
    public void testRemoveMetric() {
        Map<String, VideoPlayerMetrics> map = new HashMap<>();
        map.put(TEST_STRING, metrics);
        cache.put(TEST_LONG_1, map);
        metricsDAL.removeMetricForTimestamp(TEST_LONG_1, metrics);
        Map<String, VideoPlayerMetrics> val = cache.getIfPresent(TEST_LONG_1);
        Assertions.assertNotNull(val);
        Assertions.assertNull(val.get(metrics.getMachineId()));
    }


    @Test
    public void testHandleExpiredEntries() {

        // expired entry
        expirationMap.put(TEST_LONG_1, System.currentTimeMillis() - 10000);
        Map<String, VideoPlayerMetrics> map = new HashMap<>();
        map.put(TEST_STRING, metrics);
        map.put(TEST_STRING_2, metrics);
        cache.put(TEST_LONG_1, map);

        // non-expired entry
        expirationMap.put(TEST_LONG_2, System.currentTimeMillis() + 10000);
        cache.put(TEST_LONG_2, Map.of(TEST_STRING_2, metrics));
        metricsDAL.handleExpiredEntries();

        // assert expired entry deleted
        Assertions.assertNull(cache.getIfPresent(TEST_LONG_1));
        Assertions.assertNull(expirationMap.get(TEST_LONG_1));

        // assert non-expired entry not deleted
        Assertions.assertNotNull(cache.getIfPresent(TEST_LONG_2));
        Assertions.assertNotNull(expirationMap.get(TEST_LONG_2));
    }

}