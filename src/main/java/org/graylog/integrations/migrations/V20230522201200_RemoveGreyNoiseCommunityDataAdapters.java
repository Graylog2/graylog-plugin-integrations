package org.graylog.integrations.migrations;

import org.graylog.integrations.dataadapters.GreyNoiseCommunityIpLookupAdapter;
import org.graylog2.lookup.db.DBDataAdapterService;
import org.graylog2.lookup.dto.DataAdapterDto;
import org.graylog2.migrations.Migration;
import org.graylog2.notifications.Notification;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.system.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;

public class V20230522201200_RemoveGreyNoiseCommunityDataAdapters extends Migration {
    private static final Logger LOG = LoggerFactory.getLogger(V20230522201200_RemoveGreyNoiseCommunityDataAdapters.class);

    private final ClusterConfigService clusterConfigService;
    private final DBDataAdapterService dataAdapterService;
    private final NotificationService notificationService;
    private final NodeId nodeId;

    @Inject
    public V20230522201200_RemoveGreyNoiseCommunityDataAdapters(ClusterConfigService clusterConfigService,
                                                                DBDataAdapterService dataAdapterService,
                                                                NotificationService notificationService,
                                                                NodeId nodeId) {
        this.clusterConfigService = clusterConfigService;
        this.dataAdapterService = dataAdapterService;
        this.notificationService = notificationService;
        this.nodeId = nodeId;
    }

    /**
     * This migration removes all GreyNoise Community IP Lookup Data Adapters.
     */
    @Override
    public void upgrade() {
        if (clusterConfigService.get(V20230522201200_RemoveGreyNoiseCommunityDataAdapters.MigrationCompleted.class) != null) {
            LOG.debug("Migration already completed!");
            return;
        }

        List<DataAdapterDto> greyNoiseCommunityAdapters = dataAdapterService.findAll().stream()
                .filter(da -> da.config() instanceof GreyNoiseCommunityIpLookupAdapter).toList();

        greyNoiseCommunityAdapters.forEach(adapter -> {
            dataAdapterService.deleteAndPostEvent(adapter.id());

            final Notification systemNotification = notificationService.buildNow()
                    .addNode(nodeId.getNodeId())
                    .addType(Notification.Type.GENERIC)
                    .addSeverity(Notification.Severity.URGENT)
                    .addDetail("title", "Removed GreyNoise Community IP Lookup Data Adapter")
                    .addDetail("description", "GreyNoise Community IP Lookup Data Adapters are no longer supported.");

            notificationService.publishIfFirst(systemNotification);
        });

        clusterConfigService.write(new V20230522201200_RemoveGreyNoiseCommunityDataAdapters.MigrationCompleted());
    }

    @Override
    public ZonedDateTime createdAt() {
        return ZonedDateTime.parse("2023-05-22T20:12:00Z");
    }

    public record MigrationCompleted() {}
}