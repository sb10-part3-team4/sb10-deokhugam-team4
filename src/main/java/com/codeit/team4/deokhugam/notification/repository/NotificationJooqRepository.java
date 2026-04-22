package com.codeit.team4.deokhugam.notification.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationJooqRepository {

    private final DSLContext dsl;

}