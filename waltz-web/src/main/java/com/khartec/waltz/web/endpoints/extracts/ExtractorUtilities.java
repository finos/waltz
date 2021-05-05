package com.khartec.waltz.web.endpoints.extracts;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import spark.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.common.StringUtilities.mkSafe;


public class ExtractorUtilities {


    public static byte[] convertExcelToByteArray(XSSFWorkbook workbook) throws IOException {
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
