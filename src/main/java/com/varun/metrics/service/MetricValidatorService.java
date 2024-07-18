package com.varun.metrics.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class MetricValidatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricValidatorService.class);

    public boolean validate(String rawData) {
        if (StringUtils.isEmpty(rawData)) {
            LOGGER.error("Null or Empty data passed");
            return false;
        }

        JsonObject data;
        try {
            data = new JsonObject(rawData);
        } catch (Exception ex) {
            LOGGER.error("Exception while decoding rawData to Json", ex);
            return false;
        }

        if (!data.containsKey("utc_minute")
                || !NumberUtils.isParsable(data.getString("utc_minute"))
                || !data.containsKey("video_player")
                || StringUtils.isEmpty(data.getString("video_player"))) {
            return false;
        }

        if (data.containsKey("bitrate") && !NumberUtils.isParsable(data.getString("bitrate"))) {
            LOGGER.error("bitrate value not a numeric value.");
            return false;
        }
        if (data.containsKey("framerate") && !NumberUtils.isParsable(data.getString("framerate"))) {
            LOGGER.error("framerate value not a numeric value.");
            return false;
        }
        return true;
    }

}
