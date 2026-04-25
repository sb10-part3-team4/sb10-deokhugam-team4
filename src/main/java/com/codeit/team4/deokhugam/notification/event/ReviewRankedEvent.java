package com.codeit.team4.deokhugam.notification.event;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import java.time.LocalDate;
import java.util.UUID;

public record ReviewRankedEvent(
        UUID reviewId,
        UUID receiverId,
        PeriodType period,
        int rank,
        LocalDate snapshotDate
) {}