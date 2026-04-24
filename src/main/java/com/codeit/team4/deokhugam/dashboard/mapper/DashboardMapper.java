package com.codeit.team4.deokhugam.dashboard.mapper;

import com.codeit.team4.deokhugam.dashboard.dto.PopularBookResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PopularReviewResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PowerUserResponse;
import com.codeit.team4.deokhugam.dashboard.model.PopularBookViewModel;
import com.codeit.team4.deokhugam.dashboard.model.PopularReviewViewModel;
import com.codeit.team4.deokhugam.dashboard.model.PowerUserViewModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

    PopularBookResponse toPopularBookResponse(PopularBookViewModel model);

    PopularReviewResponse toPopularReviewResponse(PopularReviewViewModel model);

    PowerUserResponse toPowerUserResponse(PowerUserViewModel model);
}
