package com.varun.metrics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoPlayerMetrics {

    @JsonProperty("utc_minute")
    private long timestamp;

    @JsonProperty("video_player")
    private String machineId;

    @JsonProperty("framerate")
    private long framerate;

    @JsonProperty("bitrate")
    private long bitrate;

    @JsonProperty("retries")
    private int retries;
}
