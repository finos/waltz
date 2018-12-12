package com.khartec.waltz.jobs;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.function.Function;
import java.util.stream.Stream;

public class XlsUtilities {

    public static <T> T mapStrCell(Row row, int i, Function<String, T> mapper) {
        return mapper.apply(strVal(row, i));
    }


    public static String strVal(Row row, int offset) {
        return row.getCell(offset).getStringCellValue();
    }


    public static Stream<Row> streamRows(Workbook workbook, SheetNumProvider sheetDefinition) {
        Stream.Builder<Row> streamBuilder = Stream.builder();
        workbook.getSheetAt(sheetDefinition.sheetNum())
                .rowIterator()
                .forEachRemaining(r -> streamBuilder.add(r));
        return streamBuilder.build();
    }

}
