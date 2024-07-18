package com.varun.metrics.handler;

import static com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxy.METRIC_UPDATER_SERVICE_ADDRESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.varun.metrics.dal.MetricsDAL;
import com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxy;
import com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxyImpl;
import com.varun.metrics.service.MetricUpdaterService;
import com.varun.metrics.service.MetricValidatorService;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceBinder;

@ExtendWith(VertxExtension.class)
public class UDPPacketHandlerTest {

    @Mock
    private MetricUpdaterServiceProxy metricUpdaterServiceProxy;

    @Mock
    private MetricValidatorService validatorService;

    @Mock
    private JsonObject config;

    @Mock
    DatagramSocket socket;

    @Mock
    DatagramPacket packet;

    @Mock
    MetricsDAL metricsDAL;

    @Mock
    MetricUpdaterService updaterService;

    private UDPPacketHandler udpPacketHandler;

    @BeforeEach
    public void setUp(Vertx vertx, VertxTestContext testContext) throws IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        metricUpdaterServiceProxy = MetricUpdaterServiceProxyImpl.createProxy(vertx, config, updaterService);
        new ServiceBinder(vertx)
                .setAddress(METRIC_UPDATER_SERVICE_ADDRESS)
                .register(MetricUpdaterServiceProxy.class, metricUpdaterServiceProxy);

        udpPacketHandler = new UDPPacketHandler(vertx, config);
        FieldUtils.writeField(udpPacketHandler, "validatorService", validatorService, true);

        SocketAddress sender = Mockito.mock(SocketAddress.class);
        Mockito.when(packet.sender()).thenReturn(sender);
        Mockito.when(packet.sender().port()).thenReturn(123);
        Mockito.when(packet.sender().host()).thenReturn("123");
        Mockito.when(packet.data()).thenReturn(new BufferImpl());
        Mockito.when(config.getString(anyString(), anyString())).thenReturn("true");

        testContext.completeNow();
    }

    @Test
    public void testHandle_Failure(Vertx vertx, VertxTestContext testContext) {
        Mockito.when(validatorService.validate(anyString())).thenReturn(false);
        udpPacketHandler.handle(socket, packet);
        Mockito.verifyNoInteractions(config);
        Mockito.verifyNoInteractions(socket);
        testContext.completeNow();
    }

    @Test
    public void testHandle_success(Vertx vertx, VertxTestContext testContext) {
        Mockito.when(validatorService.validate(anyString())).thenReturn(true);
        udpPacketHandler.handle(socket, packet);
        Mockito.verify(config).getString(anyString(), anyString());
        Mockito.verify(socket).send(anyString(), anyInt(), anyString(), any());
        testContext.completeNow();
    }

}