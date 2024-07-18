package com.varun.metrics.handler;

import static com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxy.METRIC_UPDATER_SERVICE_ADDRESS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxy;
import com.varun.metrics.service.MetricValidatorService;

import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class UDPPacketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPPacketHandler.class);

    private MetricUpdaterServiceProxy metricUpdaterServiceProxy;

    private MetricValidatorService validatorService;

    private Vertx vertx;

    private JsonObject config;

    public UDPPacketHandler(Vertx vertx, JsonObject config) {
        metricUpdaterServiceProxy = new ServiceProxyBuilder(vertx)
                .setAddress(METRIC_UPDATER_SERVICE_ADDRESS)
                .build(MetricUpdaterServiceProxy.class);
        this.vertx = vertx;
        this.config = config;
        this.validatorService = new MetricValidatorService();
    }

    public void handle(DatagramSocket socket, DatagramPacket packet) {
        String rawData = packet.data().toString();
        LOGGER.debug("Received packet: " + packet.data().toString());
        if (validatorService.validate(rawData)) {
            metricUpdaterServiceProxy.update(rawData);
            sendAcknowledgement(socket, packet);
        } else {
            LOGGER.error("Invalid data received: {}", rawData);
        }
    }

    private void sendAcknowledgement(DatagramSocket socket, DatagramPacket packet) {
        String canSendAck = config.getString("SEND_ACK", "true");
        if (!"true".equalsIgnoreCase(canSendAck)) {
            return;
        }

        String response = "[ACK] Message received.";
        socket.send(response, packet.sender().port(), packet.sender().host(), ar -> {
            if (ar.succeeded()) {
                LOGGER.debug("Response sent");
            } else {
                LOGGER.error("Unable to send response", ar.cause());
            }
        });
    }

}
