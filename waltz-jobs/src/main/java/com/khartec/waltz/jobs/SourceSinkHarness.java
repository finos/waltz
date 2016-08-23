/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.datatype_source_sink.SourceSinkDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.datatype_source_sink.DataTypeSourceSinks;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.datatype_source_sink.SourceSinkService;
import org.jooq.DSLContext;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;


public class SourceSinkHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        SourceSinkDao sourceSinksDao = ctx.getBean(SourceSinkDao.class);
        SourceSinkService sourceSinkService = ctx.getBean(SourceSinkService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        DataTypeIdSelectorFactory dataTypeIdSelectorFactory = ctx.getBean(DataTypeIdSelectorFactory.class);

        IdSelectionOptions options = ImmutableIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference
                        .builder()
                        .kind(EntityKind.DATA_TYPE)
                        .id(6000)
                        .build())
                .scope(HierarchyQueryScope.CHILDREN)
                .build();

//        Select<Record1<Long>> dataTypeIdSelect = dataTypeIdSelectorFactory.apply(options);
//
//        List<DataTypeSourceSinks> all = sourceSinksDao.findByIdSelector(dataTypeIdSelect);

        List<DataTypeSourceSinks> all = sourceSinkService.findByIdSelector(options);

        System.out.println("got them");


    }




}
