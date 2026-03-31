package com.cibit.p2p.selector.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для вызова хранимых процедур Oracle (АБС Кибита).
 * Контракт процедур уточняется у Вити.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class P2pRepository {

    private final JdbcTemplate jdbcTemplate;

    // TODO: реализовать вызовы хранимых процедур после получения контракта от Вити
}