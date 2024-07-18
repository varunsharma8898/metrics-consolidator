package com.varun.metrics.controller.udpclient;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class UDPClientBitrateVerticle extends UDPClientVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPClientBitrateVerticle.class);

    private Random random = new Random();

    private long timestamp = 1720956451;

    @Override
    void sendMessage(DatagramSocket socket) {
        for (String machineId : MACHINE_IDS) {
            JsonObject message = getBitrateMessage(machineId);
            socket.send(Json.encodePrettily(message), 9876, "localhost", asyncResult -> {
                if (asyncResult.succeeded()) {
                    LOGGER.info("Bitrate message sent: {}", Json.encode(message));
                } else {
                    LOGGER.error("Unable to send message to server", asyncResult.cause());
                }
            });
        }
    }

    private JsonObject getBitrateMessage(String machineId) {
        int bitrate = random.nextInt(5000 - 3000 + 1) + 3000;

        JsonObject message = new JsonObject();
        message.put("video_player", machineId);
        message.put("bitrate", bitrate);
//        message.put("utc_minute", Instant.now().getEpochSecond());
        message.put("utc_minute", timestamp);
        timestamp += 10;
        return message;
    }
}
