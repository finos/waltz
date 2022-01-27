package org.finos.waltz.service;

import org.jooq.ExecuteContext;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import java.sql.SQLException;

public class SpringExceptionTranslationExecuteListener extends DefaultExecuteListener {

    private final SQLStateSQLExceptionTranslator translator;

    public SpringExceptionTranslationExecuteListener(SQLStateSQLExceptionTranslator translator) {
        this.translator = translator;
    }

    @Override
    public void exception(ExecuteContext ctx) {
        SQLException ex = ctx.sqlException();
        DataAccessException translatedEx = translator.translate("jooq", ctx.sql(), ex);
        ctx.exception(translatedEx);
    }

}