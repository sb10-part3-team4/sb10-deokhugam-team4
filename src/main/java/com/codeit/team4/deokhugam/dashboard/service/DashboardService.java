package com.codeit.team4.deokhugam.dashboard.service;

import com.codeit.team4.deokhugam.dashboard.book.PopularBookRepository;
import com.codeit.team4.deokhugam.dashboard.review.PopularReviewRepository;
import com.codeit.team4.deokhugam.dashboard.user.PowerUserRepository;
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
