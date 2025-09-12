package com.flyerme.weautotools.mapper;

import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.entity.ClickCounter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClickCounterConverter extends
        BaseConverter<ClickCounter, ClickCounterRequest, ClickCounterResponse> {
    
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "clickCount", ignore = true)
    @Mapping(target = "lastClickTime", ignore = true)
    void updateEntityFromRequest(@MappingTarget ClickCounter entity, ClickCounterRequest request);
}