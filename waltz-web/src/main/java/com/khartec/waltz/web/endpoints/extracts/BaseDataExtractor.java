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


    public abstract void register();


    protected Object writeFile(String suggestedFilename, CSVSerializer extractor, Response response) throws Exception {
        response.type(MimeTypes.Type.TEXT_PLAIN.name());
        response.header("Content-disposition", "attachment; filename="+suggestedFilename);

        StringWriter bodyWriter = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(bodyWriter, CsvPreference.EXCEL_PREFERENCE);

        extractor.accept(csvWriter);

        csvWriter.flush();
        return bodyWriter.toString();
    }

}
