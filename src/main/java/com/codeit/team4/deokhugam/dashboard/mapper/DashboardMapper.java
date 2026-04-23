package com.codeit.team4.deokhugam.dashboard.mapper;

import com.codeit.team4.deokhugam.dashboard.dto.PopularBookResponse;
import com.codeit.team4.deokhugam.dashboard.model.PopularBookViewModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

    PopularBookResponse toPopularBookResponse(PopularBookViewModel model);
}
