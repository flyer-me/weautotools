package com.flyerme.weautotools.mapper;

import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.entity.ClickCounter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClickCounterConverter extends
        BaseConverter<ClickCounter, ClickCounterRequest, ClickCounterResponse> { }
