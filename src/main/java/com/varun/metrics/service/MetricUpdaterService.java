package com.varun.metrics.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.varun.metrics.dal.MetricsDAL;
import com.varun.metrics.model.VideoPlayerMetrics;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class MetricUpdaterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricUpdaterService.class);

    private MetricsDAL metricsDal;

    private Vertx vertx;

    private JsonObject config;

    public MetricUpdaterService(Vertx vertx, JsonObject config, MetricsDAL metricsDal) {
        this.vertx = vertx;
        this.config = config;
        this.metricsDal = metricsDal;
    }

    public void updateMetric(String rawData) {
        LOGGER.debug("Config inside metric service = {}", config);

        JsonObject data = new JsonObject(rawData);

        Long timestamp = data.getLong("utc_minute");
        String machineId = data.getString("video_player");

        Single<VideoPlayerMetrics> metricsSingle = metricsDal.getMetricByTimestampAndMachineId(timestamp, machineId);
        metricsSingle.subscribe(metrics -> {
            Long bitrate = data.getLong("bitrate", -1L);
            Long framerate = data.getLong("framerate", -1L);
            if (bitrate > 0) {
                metrics.setBitrate(bitrate);
            }
            if (framerate > 0) {
                metrics.setFramerate(framerate);
            }
            metricsDal.writeMetricForTimestamp(timestamp, metrics);

            if (metrics.getBitrate() > 0 && metrics.getFramerate() > 0) {
                LOGGER.info("video_player {} is at bitrate {} and framerate {} at {}",
                        metrics.getMachineId(),
                        metrics.getBitrate(),
                        metrics.getFramerate(),
                        metrics.getTimestamp());
                metricsDal.removeMetricForTimestamp(timestamp, metrics);
            }
        });

    }

    public void handleExpiredEntries() {
        LOGGER.debug("Checking for expired entries..");
        metricsDal.handleExpiredEntries();
    }

}
