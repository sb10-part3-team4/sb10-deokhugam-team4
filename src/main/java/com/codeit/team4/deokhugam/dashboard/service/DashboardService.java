package com.codeit.team4.deokhugam.dashboard.service;

import com.codeit.team4.deokhugam.dashboard.repository.PopularBookRepository;
import com.codeit.team4.deokhugam.dashboard.repository.PopularReviewRepository;
import com.codeit.team4.deokhugam.dashboard.repository.PowerUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final PopularBookRepository popularBookRepository;
    private final PopularReviewRepository popularReviewRepository;
    private final PowerUserRepository powerUserRepository;
}
