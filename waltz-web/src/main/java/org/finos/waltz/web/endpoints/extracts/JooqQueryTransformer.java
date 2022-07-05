package org.finos.waltz.web.endpoints.extracts;

import org.finos.waltz.web.json.*;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.Select;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.FunctionUtilities.time;


public class JooqQueryTransformer {


    public GenericGridJSON transformFromQuery(ExtractSpecification extractSpecification,
                                              DSLContext dslContext) {
        return
                ImmutableGenericGridJSON.builder()
                        .id(extractSpecification.id())
                        .apiTypes(new ApiTypes())
                        .name(extractSpecification.outputName())
                        .grid(gridFor(dslContext, extractSpecification))
                        .build();
    }


    private Grid gridFor(DSLContext dslContext, ExtractSpecification extractSpecification) {
        return Optional.ofNullable(extractSpecification.qry())
                .map(q -> {
                    ImmutableGrid.Builder gridBuilder = ImmutableGrid.builder();
                    List<String> columns = columnNames(dslContext, q);
                    gridBuilder.columnDescriptors(createColumnDescriptors(columns));
                    return gridBuilder
                            .addAllRows(transformRecordsToJSON(extractSpecification, columns, query(dslContext, q)))
                            .build();
                })
                .orElseThrow(() -> new IllegalArgumentException("Query not supplied"));
    }


    private List<ColumnDescriptor> createColumnDescriptors(List<String> columns){
        AtomicInteger idx = new AtomicInteger();
        return columns.stream()
                .map(name -> ImmutableColumnDescriptor.builder()
                        .name(name)
                        .id(""+idx.getAndIncrement())
                        .build())
                .collect(Collectors.toList());

    }

    private List<ImmutableRow> transformRecordsToJSON(ExtractSpecification extractSpecification,List<String> colNames, Result<?> records){
        List<ImmutableRow> jsonRows = new ArrayList<>(records.size());

        int colCount = colNames.size();
        AtomicInteger rowCount = new AtomicInteger();
        records.forEach(r -> {
            ImmutableRow.Builder row = ImmutableRow.builder();
            List<CellValue> cellValues = new ArrayList<>(colCount);
            row.id(idOf(rowCount.getAndIncrement()));
            for (int col = 0; col < colCount; col++) {
                Object val = r.get(col);
                if(val!=null) {
                    ImmutableCellValue.Builder cellBuilder = ImmutableCellValue.builder();
                    cellBuilder.name(ExtractFormat.JSON.equals(extractSpecification.extractFormat()) ? colNames.get(col) : ""+col);
                    cellBuilder.value(val.toString());
                    cellValues.add(cellBuilder.build());
                }
            }
            row.addAllCells(cellValues);
            jsonRows.add(row.build());
        });
        return jsonRows;
    }

    private static SimpleKeyCell idOf(int id){
        return ImmutableSimpleKeyCell.builder()
                .name(Integer.toString(id)).build();
    }

    private List<String> columnNames(DSLContext dslContext, Select<?> qry) {
        return qry.fieldStream()
                .map (f -> Objects.toString(f.getName()))
                .collect(Collectors.toList());
    }

    private Result<?> query(DSLContext dslContext, Select<?> qry){
        return dslContext == null
                ? qry.fetch()
                : time("fetch", () -> dslContext.fetch(dslContext.renderInlined(qry)));
    }
}
