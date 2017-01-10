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

package com.khartec.waltz.data.capability.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.model.capability.Capability;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class PostgresCapabilitySearch implements FullTextSearch<Capability>, DatabaseVendorSpecific {


    private static final String QUERY = "SELECT\n" +
            "  *,\n" +
            "  ts_rank_cd(\n" +
            "      setweight(to_tsvector(name), 'A')\n" +
            "      || setweight(to_tsvector(description), 'D'),\n" +
            "      plainto_tsquery(?)) AS rank\n" +
            "FROM capability\n" +
            "WHERE\n" +
            "  setweight(to_tsvector(name), 'A')\n" +
            "  || setweight(to_tsvector(description), 'D')\n" +
            "  @@ plainto_tsquery(?)\n" +
            "ORDER BY rank DESC\n" +
            "LIMIT 20;\n";


    @Override
    public List<Capability> search(DSLContext dsl, String terms) {
        Result<Record> records = dsl.fetch(QUERY, terms, terms);
        return records.map(CapabilityDao.TO_DOMAIN_MAPPER);
    }

}
