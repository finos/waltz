package org.finos.waltz.web.endpoints.extracts;

import org.finos.waltz.web.MimeTypes;
import org.jooq.lambda.tuple.Tuple3;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.finos.waltz.common.EnumUtilities.readEnum;

/**
 * Represents classes which are used for Data Extraction.
 */
public interface DataExtractor {

    void register();


    /**
     * Reads the extract format from the web request.  Expects as a param with the
     * name `format`.  E.g. `my-extract?format=XLSX`
     *
     * Defaults to` ExtractFormat.CSV` if not found or not recognized.
     *
     * @param request  web request
     * @return enum representing the value of the format parameter
     */
    default ExtractFormat parseExtractFormat(Request request) {
        return readEnum(
                request.queryParams("format"),
                ExtractFormat.class,
                v -> ExtractFormat.CSV);
    }


    default Object writeReportResults(Response response, Tuple3<ExtractFormat, String, byte[]> reportResult) throws IOException {
        String templateName = reportResult.v2;

        HttpServletResponse httpResponse = response.raw();

        switch (reportResult.v1) {
            case CSV:
                response.type(MimeTypes.TEXT_PLAIN);
                response.header("Content-disposition", "attachment; filename=" + templateName + ".csv");
                break;
            case JSON:
                httpResponse.setHeader("Content-Type", MimeTypes.APPLICATION_JSON_UTF_8);
                break;
            case XLSX:
                httpResponse.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                httpResponse.setHeader("Content-Disposition", "attachment; filename=" + templateName + ".xlsx");
                httpResponse.setHeader("Content-Transfer-Encoding", "7bit");
                break;
            default:
                break;
        }

        byte[] bytes = reportResult.v3;
        httpResponse.setContentLength(bytes.length);
        httpResponse.getOutputStream().write(bytes);
        httpResponse.getOutputStream().flush();
        httpResponse.getOutputStream().close();
        return httpResponse;
    }


}
