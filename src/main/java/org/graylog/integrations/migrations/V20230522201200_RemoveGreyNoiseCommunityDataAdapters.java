package org.graylog.integrations.migrations;

import org.graylog.integrations.dataadapters.GreyNoiseCommunityIpLookupAdapter;
import org.graylog2.lookup.db.DBDataAdapterService;
import org.graylog2.lookup.dto.DataAdapterDto;
import org.graylog2.migrations.Migration;
import org.graylog2.notifications.Notification;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.shared.utilities.StringUtils;
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

    @Inject
    public V20230522201200_RemoveGreyNoiseCommunityDataAdapters(ClusterConfigService clusterConfigService,
                                                                DBDataAdapterService dataAdapterService,
                                                                NotificationService notificationService) {
        this.clusterConfigService = clusterConfigService;
        this.dataAdapterService = dataAdapterService;
        this.notificationService = notificationService;
    }

    /**
     * This migration notifies users of the deprecation and removal of functionality for GreyNoiseCommunityIpLookupAdapters.
     */
    @Override
    public void upgrade() {
        if (clusterConfigService.get(V20230522201200_RemoveGreyNoiseCommunityDataAdapters.MigrationCompleted.class) != null) {
            LOG.debug("Migration already completed!");
            return;
        }

        List<DataAdapterDto> greyNoiseCommunityAdapters = dataAdapterService.findAll().stream()
                .filter(da -> da.config().type().equals(GreyNoiseCommunityIpLookupAdapter.ADAPTER_NAME)).toList();

        greyNoiseCommunityAdapters.forEach(adapter -> {
            final Notification systemNotification = notificationService.buildNow()
                    .addType(Notification.Type.GENERIC)
                    .addSeverity(Notification.Severity.URGENT)
                    .addDetail("title", StringUtils.f("Disabled Data Adapter [%s]", adapter.title()))
                    .addDetail("description", "GreyNoise Community IP Lookup Data Adapters are no longer supported as of Graylog 5.1.3."
                            + "This Data Adapter's lookups will no longer return results and it should be deleted");

            notificationService.publishIfFirst(systemNotification);
        });

        //clusterConfigService.write(new V20230522201200_RemoveGreyNoiseCommunityDataAdapters.MigrationCompleted());
    }

    @Override
    public ZonedDateTime createdAt() {
        return ZonedDateTime.parse("2023-05-22T20:12:00Z");
    }

    public record MigrationCompleted() {}
}