/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.*;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;

/**
 * An example of a report showing logical flows,
 * alongside authoritativeness, LDE mappings and OLA bookmarks
 **/
public class FlowSummaryExport {

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        LogicalFlow logFlow = LogicalFlow.LOGICAL_FLOW.as("logFlow");
        PhysicalFlow physFlow = PhysicalFlow.PHYSICAL_FLOW.as("physFlow");
        AuthoritativeSource auth = AuthoritativeSource.AUTHORITATIVE_SOURCE.as("auth");
        Application src = Application.APPLICATION.as("src");
        Application trg = Application.APPLICATION.as("trg");
        DataType dt = DataType.DATA_TYPE.as("dt");
        LogicalFlowDecorator dec = LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR.as("dec");

        Set<Long> logicalFlowsWithOLAs = getLogicalFlowsWithOLAs(dsl, logFlow, physFlow);
        Set<Long> logicalFlowsWithLDEs = getLogicalFlowsWithLDEs(dsl, logFlow, physFlow);

        FileWriter fw = new FileWriter("c:\\temp\\out.tsv");

        Consumer<Tuple> dumper = Unchecked.consumer(t -> {
            t.forEach(Unchecked.consumer(v -> {
                fw.write(v.toString());
                fw.write("\t");
            }));
            fw.write("\n");
        });

        fw.write("Source\tSource code\tTarget\tTarget code\tData Type\tData Type Code\tAuthoritative\tLDE\tOLA\n");

        dsl.select(src.NAME,
                    src.ASSET_CODE,
                    trg.NAME,
                    trg.ASSET_CODE,
                    dt.NAME,
                    dt.CODE,
                    dec.RATING,
                    logFlow.ID)
                .from(logFlow)
                .innerJoin(dec)
                    .on(dec.LOGICAL_FLOW_ID.eq(logFlow.ID))
                .innerJoin(dt)
                    .on(dt.ID.eq(dec.DECORATOR_ENTITY_ID))
                .innerJoin(src)
                    .on(logFlow.SOURCE_ENTITY_ID.eq(src.ID))
                .innerJoin(trg)
                    .on(logFlow.TARGET_ENTITY_ID.eq(trg.ID))
                .where(logFlow.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name())
                    .and(src.IS_REMOVED.isFalse())
                    .and(trg.IS_REMOVED.isFalse()))
                .fetch()
                .stream()
                .map(r -> {
                    Long logicalFlowId = r.get(logFlow.ID);
                    return Tuple.tuple(
                            r.get(src.NAME),
                            r.get(src.ASSET_CODE),
                            r.get(trg.NAME),
                            r.get(trg.ASSET_CODE),
                            r.get(dt.NAME),
                            r.get(dt.CODE),
                            authRatingToStr(r.get(dec.RATING)),
                            logicalFlowsWithLDEs.contains(logicalFlowId) ? 1 : 0,
                            logicalFlowsWithOLAs.contains(logicalFlowId) ? 1 : 0);
                })
                .forEach(dumper);

        fw.close();
    }

    private static Set<Long> getLogicalFlowsWithLDEs(DSLContext dsl,
                                                     LogicalFlow logFlow,
                                                     PhysicalFlow physFlow) {
        PhysicalSpecDefnField field = PhysicalSpecDefnField.PHYSICAL_SPEC_DEFN_FIELD.as("field");

        return dsl
                .select(logFlow.ID)
                .from(logFlow)
                .innerJoin(physFlow)
                .on(physFlow.LOGICAL_FLOW_ID.eq(logFlow.ID))
                .innerJoin(field)
                .on(field.SPEC_DEFN_ID.eq(physFlow.SPECIFICATION_DEFINITION_ID))
                .where(field.LOGICAL_DATA_ELEMENT_ID.isNotNull())
                .and(logFlow.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()))
                .fetch(logFlow.ID)
                .stream()
                .collect(Collectors.toSet());
    }


    private static Set<Long> getLogicalFlowsWithOLAs(DSLContext dsl,
                                                     LogicalFlow logFlow,
                                                     PhysicalFlow physFlow) {
        Bookmark bk = Bookmark.BOOKMARK.as("bk");
        return dsl
                .select(logFlow.ID)
                .from(logFlow)
                .innerJoin(physFlow)
                    .on(physFlow.LOGICAL_FLOW_ID.eq(logFlow.ID))
                .innerJoin(bk)
                    .on(bk.PARENT_ID.eq(physFlow.ID)
                        .and(bk.PARENT_KIND.eq(EntityKind.PHYSICAL_FLOW.name())))
                .where(bk.KIND.eq("OLA"))
                .and(logFlow.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()))
                .fetch(logFlow.ID)
                .stream()
                .collect(Collectors.toSet());
    }


    private static String authRatingToStr(String r) {
        switch(r) {
            case "PRIMARY":
                return "RAS";
            case "SECONDARY":
                return "Non RAS";
            case "DISCOURAGED":
                return "Non Auth";
            case "NO_OPINION":
            default:
                return "No Opinion";
        }
    }


}
