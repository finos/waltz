package org.finos.waltz.service.report_grid;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class ReportGridEvaluatorNamespace {

    private Map<String, Object> ctx = new HashMap<>();

    public void setContext(Map<String, Object> ctx) {
        this.ctx = ctx;
    }

    public void addContext(String key, Object value) {
        ctx.put(key, value);
    }

    public Object cell(String cellExtId) {
        Object object = ctx.get(cellExtId);

        if(object == null) {
            throw new IllegalArgumentException(format("Cannot find cell: '%s' in context", cellExtId));
        } else {
            return object;
        }
    }

    public Map<String, Object> getContext() {
        return ctx;
    }
}
