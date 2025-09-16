package com.flyerme.weautotools.mapper;

import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.entity.ClickCounter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClickCounterConverter extends
        BaseConverter<ClickCounter, ClickCounterRequest, ClickCounterResponse> {
    
    @Override
    void updateEntityFromRequest(@MappingTarget ClickCounter entity, ClickCounterRequest request);
}