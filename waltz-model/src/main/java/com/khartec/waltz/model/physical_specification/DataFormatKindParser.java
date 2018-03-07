package com.khartec.waltz.model.physical_specification;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.common.EnumParser;

public class DataFormatKindParser extends EnumParser<DataFormatKind> {

    private static final Aliases<DataFormatKind> defaultAliases = new Aliases<>()
            .register(DataFormatKind.FLAT_FILE,
                    "CSV_FILE", "CSV", "EXCEL", "PDF", "EXCEL/PDF",
                    "FILE", "TEXT", "XL", "XLS", "XLM", "PIPE DELIMITED")
            .register(DataFormatKind.DATABASE, "DATA")
            .register(DataFormatKind.UNSTRUCTURED, "EMAIL", "STRING", "TXT")
            .register(DataFormatKind.OTHER, "OTHER", "JOB_ROW_CRM")
            .register(DataFormatKind.XML, "Webservice");


    public DataFormatKindParser() {
        super(defaultAliases, DataFormatKind.class);
    }
}