package com.dvgs.notification.repository;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.NotificationLog;
import com.dvgs.notification.domain.NotificationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByRequestIdOrderByLastAttemptAtDesc(String requestId);
    List<NotificationLog> findByProviderMessageId(String providerMessageId);

    @Query("SELECT l.channel as channel, l.status as status, COUNT(l) as total FROM NotificationLog l GROUP BY l.channel, l.status")
    List<NotificationChannelStatsView> aggregateChannelStats();

    interface NotificationChannelStatsView {
        ChannelType getChannel();
        NotificationStatus getStatus();
        long getTotal();
    }
}
