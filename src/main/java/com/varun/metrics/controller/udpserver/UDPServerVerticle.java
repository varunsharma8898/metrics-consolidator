package com.varun.metrics.controller.udpserver;

import static com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxy.METRIC_UPDATER_SERVICE_ADDRESS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxy;
import com.varun.metrics.handler.UDPPacketHandler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class UDPServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPServerVerticle.class);

    UDPPacketHandler udpPacketHandler;

    MetricUpdaterServiceProxy serviceProxy;

//    ObjectFactory factory;

    public UDPServerVerticle() {
    }

    @Override
    public void start() {

        serviceProxy = new ServiceProxyBuilder(vertx)
                .setAddress(METRIC_UPDATER_SERVICE_ADDRESS)
                .build(MetricUpdaterServiceProxy.class);

        udpPacketHandler = new UDPPacketHandler(vertx, config());

        DatagramSocketOptions options = new DatagramSocketOptions();
        DatagramSocket socket = vertx.createDatagramSocket(options);

        socket.listen(9876, "0.0.0.0", asyncResult -> {
            if (asyncResult.succeeded()) {
                LOGGER.info("UDP server is listening on port 9876");
                socket.handler(packet -> udpPacketHandler.handle(socket, packet));
            } else {
                LOGGER.error("Unable to listen to port {}", 9876, asyncResult.cause());
            }
        });

        DatagramSocket socket2 = vertx.createDatagramSocket(options);
        socket2.listen(9877, "0.0.0.0", asyncResult -> {
            if (asyncResult.succeeded()) {
                socket2.handler(packet -> udpPacketHandler.handle(socket, packet));
            } else {
                LOGGER.error("Unable to listen to port {}", 9877, asyncResult.cause());
            }
        });

        vertx.setPeriodic(2000, id -> {
            serviceProxy.handleExpiredEntries();
        });

    }
}
