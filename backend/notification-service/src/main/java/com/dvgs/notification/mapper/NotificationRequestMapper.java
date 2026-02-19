package com.dvgs.notification.mapper;

import com.dvgs.notification.domain.NotificationRequest;
import com.dvgs.notification.dto.NotificationRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "preferredChannels", ignore = true)
    @Mapping(target = "payload", ignore = true)
    NotificationRequest toEntity(NotificationRequestDto dto);
}
