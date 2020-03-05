package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.DateTimeUtilities
 *
 * @author Diffblue JCover
 */

public class DateTimeUtilitiesTest {

    @Test
    public void today() {
        assertThat(DateTimeUtilities.today(), equalTo(LocalDate.now()));
    }

    @Test
    public void toLocalDate() {
        assertThat(DateTimeUtilities.toLocalDate((Date)null), is(nullValue()));
        assertThat(DateTimeUtilities.toLocalDate((Timestamp)null), is(nullValue()));
    }

    @Test
    public void toLocalDateTimeDateIsNullReturnsNull() {
        assertThat(DateTimeUtilities.toLocalDateTime(null), is(nullValue()));
    }

    @Test
    public void toSqlDate() {
        assertThat(DateTimeUtilities.toSqlDate(LocalDate.of(2_000, 1, 1)), equalTo(new java.sql.Date(946_684_800_000L)));
        assertThat(DateTimeUtilities.toSqlDate(new Date(1L)), equalTo(new java.sql.Date(1L)));
        assertThat(DateTimeUtilities.toSqlDate((Date)null), is(nullValue()));
        assertThat(DateTimeUtilities.toSqlDate((LocalDate)null), is(nullValue()));
    }
}
