package com.varun.metrics.controller.udpclient;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;

public abstract class UDPClientVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPClientVerticle.class);

    static final List<String> MACHINE_IDS = List.of(
//            "1dc71b78-5256-4221-94cb-944b0104eb6c",
//            "470b8f5b-5537-4a32-814e-626613663ead",
//            "16f432d8-26a5-4360-8749-acc0a8b87f13",
//            "87dac300-e325-4b35-a23c-0a7f9f877321",
//            "2e0a96f5-a05b-4069-bbf3-9d95f3ecf626",
//            "3e11756c-cae1-408e-9ab2-6f1eace1efcc",
//            "e236a010-7d6e-4a25-8dd9-f1d094d46eaf",
//            "d7cce8c2-f46a-4f57-9f6a-ae8a3fe9f26e",
//            "d77749ad-208f-4545-bc4b-b85a6b2592f8",
            "95887462-079b-4b4f-9030-fe7fb6341158"
    );

    @Override
    public void start() {
        DatagramSocketOptions options = new DatagramSocketOptions();
        DatagramSocket socket = vertx.createDatagramSocket(options);

        // produce bitrate message after every X milliseconds
        vertx.setPeriodic(2000, id -> {
            sendMessage(socket);
        });

        socket.handler(packet -> {
            LOGGER.debug("Received response: {}", packet.data());
        });
    }

    abstract void sendMessage(DatagramSocket socket);

}
