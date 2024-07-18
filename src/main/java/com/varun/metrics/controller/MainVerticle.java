package com.varun.metrics.controller;

import static com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxy.METRIC_UPDATER_SERVICE_ADDRESS;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.varun.metrics.controller.udpclient.UDPClientBitrateVerticle;
import com.varun.metrics.controller.udpclient.UDPClientFramerateVerticle;
import com.varun.metrics.controller.udpserver.UDPServerVerticle;
import com.varun.metrics.dal.MetricsDAL;
import com.varun.metrics.dal.MetricsDALImpl;
import com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxy;
import com.varun.metrics.di.metricupdater.MetricUpdaterServiceProxyImpl;
import com.varun.metrics.service.MetricUpdaterService;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private static final String DEPLOYMENT_SERVER = "server";

    private static final String DEPLOYMENT_BITRATE_CLIENT = "bitrate_client";

    private static final String DEPLOYMENT_FRAMERATE_CLIENT = "framerate_client";

    private static final String DEPLOYMENT_ALL = "all";

    private static final Set<String> DEPLOYMENT_TYPES = Set.of(
            DEPLOYMENT_SERVER, DEPLOYMENT_BITRATE_CLIENT, DEPLOYMENT_FRAMERATE_CLIENT, DEPLOYMENT_ALL
    );

    public static void main(String[] args) {
        String deploymentType = args.length > 0 ? args[0] : DEPLOYMENT_ALL;
        if (!DEPLOYMENT_TYPES.contains(deploymentType)) {
            LOGGER.error("Unsupported deployment type: {}, please pass one of the following values: {}", deploymentType, DEPLOYMENT_TYPES);
            return;
        }

        // Initialize Vertx instance
        Vertx vertx = Vertx.vertx();

        // Get config from a properties file
        ConfigStoreOptions fileOptions = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setConfig(new JsonObject().put("path", "conf/conf.properties"));
        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(fileOptions);
        ConfigRetriever confRetriever = ConfigRetriever.create(vertx, options);
        Future<JsonObject> configFuture = confRetriever.getConfig();

        configFuture.onComplete(appConfig -> {
            JsonObject config = appConfig.result();
            config.put("DEPLOYMENT_TYPE", deploymentType);

            // Bind the metrics updater service to the current verticle
            MetricsDAL metricsDAL = new MetricsDALImpl();
            MetricUpdaterService updaterService = new MetricUpdaterService(vertx, config, metricsDAL);
            MetricUpdaterServiceProxy serviceProxy = MetricUpdaterServiceProxyImpl.createProxy(vertx, config, updaterService);
            new ServiceBinder(vertx)
                    .setAddress(METRIC_UPDATER_SERVICE_ADDRESS)
                    .register(MetricUpdaterServiceProxy.class, serviceProxy);

            // Deploy the UDP server verticle
            if (DEPLOYMENT_SERVER.equalsIgnoreCase(deploymentType) || DEPLOYMENT_ALL.equalsIgnoreCase(deploymentType)) {
                LOGGER.info("Deploying UPD Server Verticle for deployment type {}.", deploymentType);
                vertx.deployVerticle(new UDPServerVerticle());
            }

            // Deploy the UDP client verticle
            if (DEPLOYMENT_BITRATE_CLIENT.equalsIgnoreCase(deploymentType) || DEPLOYMENT_ALL.equalsIgnoreCase(deploymentType)) {
                LOGGER.info("Deploying Bitrate Client Verticle for deployment type {}.", deploymentType);
                vertx.deployVerticle(new UDPClientBitrateVerticle());
            }
            if (DEPLOYMENT_FRAMERATE_CLIENT.equalsIgnoreCase(deploymentType) || DEPLOYMENT_ALL.equalsIgnoreCase(deploymentType)) {
                LOGGER.info("Deploying Framerate Client Verticle for deployment type {}.", deploymentType);
                vertx.deployVerticle(new UDPClientFramerateVerticle());
            }
        });
    }
}
