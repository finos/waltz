package com.khartec.waltz.web.endpoints.extracts;

import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BinaryDataBasedDataExtractor implements DataExtractor {

    protected Object writeExtract(String suggestedFilenameStem,
                                  byte[] dataBytes,
                                  Request request,
                                  Response response) throws IOException {
        ExtractFormat format = parseExtractFormat(request);
        switch (format) {
            case SVG:
                return writeSvg(suggestedFilenameStem, dataBytes, response);
            default:
                throw new IllegalArgumentException("Cannot write extract using format: " + format);
        }
    }


    private Object writeSvg(String suggestedFilenameStem,
                            byte[] dataBytes,
                            Response response) throws IOException {
        HttpServletResponse httpResponse = response.raw();

        httpResponse.setHeader("Content-Type", "image/svg+xml");
        httpResponse.setHeader("Content-Disposition", "attachment; filename=" + suggestedFilenameStem + ".svg");
        httpResponse.setHeader("Content-Transfer-Encoding", "7bit");

        httpResponse.setContentLength(dataBytes.length);
        httpResponse.getOutputStream().write(dataBytes);
        httpResponse.getOutputStream().flush();
        httpResponse.getOutputStream().close();

        return httpResponse;
    }
}
