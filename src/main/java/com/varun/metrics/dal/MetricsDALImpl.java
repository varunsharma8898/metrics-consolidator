package com.varun.metrics.dal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.varun.metrics.model.VideoPlayerMetrics;

import io.reactivex.Single;

public class MetricsDALImpl implements MetricsDAL {

    private static final int MAX_CACHE_SIZE = 100000;

    private static final int TTL_MINUTES = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsDALImpl.class);

    private static final int MAX_RETRIES = 3;

    /**
     * Using google guava caching here with TTL for expiring entries.
     *
     * Entries are deleted once their data from both the streams has been processed for a particular timestamp.
     * If an entry is not received for a particular timestamp (either bitrate or framerate) before TTL is reached,
     * it'll be expired and removed from caches.
     *
     * Added another map for a way to send expiring messages to retry or dead-letter queues.
     *
     * Clean-up happens every 2 seconds. This can be made configurable.
     * */
    private Cache<Long, Map<String, VideoPlayerMetrics>> cache;

    private ConcurrentHashMap<Long, Long> expirationMap;

    public MetricsDALImpl() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(TTL_MINUTES + 1, TimeUnit.MINUTES)
                .build();
        this.expirationMap = new ConcurrentHashMap<>();
    }

    @Override
    public void writeMetricForTimestamp(Long timestamp, VideoPlayerMetrics metric) {
        Map<String, VideoPlayerMetrics> valueMap = cache.getIfPresent(timestamp);
        if (valueMap == null) {
            valueMap = new HashMap<>();
        }
        valueMap.put(metric.getMachineId(), metric);

        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(TTL_MINUTES);

        cache.put(timestamp, valueMap);
        expirationMap.put(timestamp, expirationTime);
    }

    @Override
    public Single<VideoPlayerMetrics> getMetricByTimestampAndMachineId(Long timestamp, String machineId) {
        Map<String, VideoPlayerMetrics> valueMap = cache.getIfPresent(timestamp);
        if (valueMap != null && valueMap.containsKey(machineId)) {
            return Single.just(valueMap.get(machineId));
        }
        return Single.just(VideoPlayerMetrics.builder()
                .timestamp(timestamp)
                .machineId(machineId)
                .build());
    }

    @Override
    public void removeMetricForTimestamp(Long timestamp, VideoPlayerMetrics metric) {
        Map<String, VideoPlayerMetrics> valueMap = cache.getIfPresent(timestamp);
        String key = metric.getMachineId();
        if (valueMap != null) {
            valueMap.remove(key);
            cache.put(timestamp, valueMap);
        }
    }

    @Override
    public void handleExpiredEntries() {
        LOGGER.debug("Checking for expired entries..");
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Long, Long> entry : expirationMap.entrySet()) {
            Long key = entry.getKey();
            if (entry.getValue() <= currentTime) {
                onEntryExpired(key);
                cache.invalidate(key);
                expirationMap.remove(key);
            }
        }
        LOGGER.debug("Expired entries cleared.");
    }

    /**
     * This method can be used to redirect expired entries to a delayed retry queue.
     * Note: Increment the number of retries in VideoPlayerMetrics before pushing.
     * If the retry count has reached the maximum allowable limit, discard the entry by pushing it to a dead-letter queue.
     */
    private void onEntryExpired(Long key) {
        Map<String, VideoPlayerMetrics> valueMap = cache.getIfPresent(key);
        if (valueMap == null) {
            return;
        }
        for (VideoPlayerMetrics metrics : valueMap.values()) {
            LOGGER.info("Entry expired after {} minutes. Entry: {}", TTL_MINUTES, metrics);
            metrics.setRetries(metrics.getRetries() + 1);
            if (metrics.getRetries() >= MAX_RETRIES) {
                LOGGER.info("Unable to process entry after {} retries, discarding to a dead-letter queue.", MAX_RETRIES);
                // push to a dead-letter queue
            } else {
                LOGGER.info("Unable to process entry - try {}, pushing to a retry queue for delayed processing.", metrics.getRetries());
                // push to a retry queue
            }
        }
    }
}
