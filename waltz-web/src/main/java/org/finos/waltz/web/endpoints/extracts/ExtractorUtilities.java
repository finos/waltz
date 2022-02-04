package org.finos.waltz.web.endpoints.extracts;


import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import spark.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.finos.waltz.common.EnumUtilities.readEnum;
import static org.finos.waltz.common.StringUtilities.mkSafe;


public class ExtractorUtilities {


    public static byte[] convertExcelToByteArray(SXSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        workbook.write(outByteStream);
        workbook.close();
        return outByteStream.toByteArray();
    }

    /**
     * Reads the extract format from the web request.  Expects as a param with the
     * name `format`.  E.g. `my-extract?format=XLSX`
     *
     * Defaults to` ExtractFormat.CSV` if not found or not recognized.
     *
     * @param request  web request
     * @return enum representing the value of the format parameter
     */
    public static ExtractFormat parseExtractFormat(Request request) {
        return readEnum(
                request.queryParams("format"),
                ExtractFormat.class,
                v -> ExtractFormat.CSV);
    }


    /**
     * Removes illegal characters from sheetName.  Currently: `\ : ; * ? / `
     *
     * @param name  input name of the sheet
     * @return outputs sanitized sheet name
     */
    public static String sanitizeSheetName(String name) {
        return mkSafe(name).replaceAll("[:;*?/\\\\]", "");
    }

}
