package com.varun.metrics.dal;

import com.varun.metrics.model.VideoPlayerMetrics;

import io.reactivex.Single;

public interface MetricsDAL {

    void writeMetricForTimestamp(Long timestamp, VideoPlayerMetrics metric);

    Single<VideoPlayerMetrics> getMetricByTimestampAndMachineId(Long timestamp, String machineId);

    void removeMetricForTimestamp(Long timestamp, VideoPlayerMetrics metric);

    void handleExpiredEntries();
}
