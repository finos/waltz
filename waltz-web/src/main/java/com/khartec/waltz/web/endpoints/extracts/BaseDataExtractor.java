package com.khartec.waltz.web.endpoints.extracts;


import org.eclipse.jetty.http.MimeTypes;
import org.jooq.DSLContext;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import spark.Response;

import java.io.StringWriter;

import static com.khartec.waltz.common.Checks.checkNotNull;


public abstract class BaseDataExtractor {

    protected DSLContext dsl;


    public BaseDataExtractor(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public abstract void register(String baseUrl);


    protected String writeFile(String suggestedFilename, CSVSerializer extractor, Response response) throws Exception {
        response.type(MimeTypes.Type.TEXT_PLAIN.name());
        response.header("Content-disposition", "attachment; filename="+suggestedFilename);

        StringWriter bodyWriter = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(bodyWriter, CsvPreference.EXCEL_PREFERENCE);

        extractor.accept(csvWriter);

        csvWriter.flush();
        return bodyWriter.toString();
    }

}
