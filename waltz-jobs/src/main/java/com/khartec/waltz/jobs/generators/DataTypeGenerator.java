/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.generators;

import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;


public class DataTypeGenerator implements SampleDataGenerator {

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = getDsl(ctx);

        dsl.deleteFrom(DATA_TYPE).execute();

        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "BOOK")
                .set(DATA_TYPE.DESCRIPTION, "Book Data")
                .set(DATA_TYPE.NAME, "Book Data")
                .set(DATA_TYPE.ID, 1000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "PARTY")
                .set(DATA_TYPE.DESCRIPTION, "Party Data")
                .set(DATA_TYPE.NAME, "Counterparty etc.")
                .set(DATA_TYPE.ID, 2000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "PRICING")
                .set(DATA_TYPE.DESCRIPTION, "Pricing Data")
                .set(DATA_TYPE.NAME, "Pricing Data")
                .set(DATA_TYPE.ID, 3000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "TRADE")
                .set(DATA_TYPE.DESCRIPTION, "Trade Data")
                .set(DATA_TYPE.NAME, "Transactions etc.")
                .set(DATA_TYPE.ID, 4000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "RATE")
                .set(DATA_TYPE.DESCRIPTION, "Rates")
                .set(DATA_TYPE.NAME, "Interest rates etc")
                .set(DATA_TYPE.ID, 5000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "CURRENCY")
                .set(DATA_TYPE.DESCRIPTION, "Currency")
                .set(DATA_TYPE.NAME, "Currencies etc")
                .set(DATA_TYPE.ID, 6000L)
                .execute();
        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "VIRTUAL_CURRENCIES")
                .set(DATA_TYPE.DESCRIPTION, "Virtual Currencies eg: Bitcoin")
                .set(DATA_TYPE.NAME, "Virtual Currencies")
                .set(DATA_TYPE.ID, 6100L)
                .set(DATA_TYPE.PARENT_ID, 6000L)
                .execute();

        dsl.insertInto(DATA_TYPE)
                .set(DATA_TYPE.CODE, "UNKNOWN")
                .set(DATA_TYPE.DESCRIPTION, "Unknown")
                .set(DATA_TYPE.NAME, "Unknown")
                .set(DATA_TYPE.ID, 1L)
                .set(DATA_TYPE.UNKNOWN, true)
                .execute();

        return null;
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx).deleteFrom(DATA_TYPE).execute();
        return true;
    }
}
