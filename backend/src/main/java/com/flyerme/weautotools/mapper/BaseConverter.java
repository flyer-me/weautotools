package com.flyerme.weautotools.mapper;

import com.flyerme.weautotools.common.BaseEntity;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

public interface BaseConverter<T extends BaseEntity, R, S> {
    T toEntity(R dto);

    S toResponse(T entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    // @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    // 注：updatedAt 会在BaseServiceImpl中设置
    void updateEntityFromRequest(@MappingTarget T entity, R request);
}