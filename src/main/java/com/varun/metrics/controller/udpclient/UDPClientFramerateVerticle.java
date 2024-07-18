package com.varun.metrics.controller.udpclient;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class UDPClientFramerateVerticle extends UDPClientVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPClientFramerateVerticle.class);

    private Random random = new Random();

    private long timestamp = 1720956451;

    @Override
    void sendMessage(DatagramSocket socket) {
        for (String machineId : MACHINE_IDS) {
            JsonObject message = getFramerateMessage(machineId);
            socket.send(Json.encodePrettily(message), 9877, "localhost", asyncResult -> {
                if (asyncResult.succeeded()) {
                    LOGGER.info("Framerate message sent: {}", Json.encode(message));
                } else {
                    LOGGER.error("Unable to send message to server", asyncResult.cause());
                }
            });
        }
    }

    private JsonObject getFramerateMessage(String machineId) {
        int framerate = random.nextInt(60 - 20 + 1) + 20;

        JsonObject message = new JsonObject();
        message.put("video_player", machineId);
        message.put("framerate", framerate);
//        message.put("utc_minute", Instant.now().getEpochSecond());
        message.put("utc_minute", timestamp);
        timestamp += 10;
        return message;
    }

}
