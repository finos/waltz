/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.access_log;

import org.finos.waltz.data.access_log.AccessLogDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class AccessLogServiceDateRangeTest {

    @Mock
    private AccessLogDao accessLogDao;

    private AccessLogService service;

    private final LocalDate start = LocalDate.of(2024, 1, 1);
    private final LocalDate end = LocalDate.of(2024, 6, 1);

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new AccessLogService(accessLogDao);
    }

    @Test
    public void validRangeIsPassedToDao() {
        when(accessLogDao.findActivityByHourOfDay(start, end)).thenReturn(Collections.emptyList());
        service.findActivityByHourOfDay(start, end);
        verify(accessLogDao).findActivityByHourOfDay(start, end);
    }

    @Test
    public void startAfterEndIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.findActivityByHourOfDay(end, start));
        verifyNoInteractions(accessLogDao);
    }

    @Test
    public void nullDatesAreAllowed() {
        when(accessLogDao.findActivityByHourOfDay(null, null)).thenReturn(Collections.emptyList());
        service.findActivityByHourOfDay(null, null);
        verify(accessLogDao).findActivityByHourOfDay(null, null);
    }
}
