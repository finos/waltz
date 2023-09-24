package org.finos.waltz.service;

import org.jooq.ExecuteContext;
import org.jooq.impl.DefaultExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import java.sql.SQLException;

import static java.lang.String.format;

public class SpringExceptionTranslationExecuteListener extends DefaultExecuteListener {

    private static final Logger LOG = LoggerFactory.getLogger(SpringExceptionTranslationExecuteListener.class);
    private final SQLStateSQLExceptionTranslator translator;

    public SpringExceptionTranslationExecuteListener(SQLStateSQLExceptionTranslator translator) {
        this.translator = translator;
    }

    @Override
    public void exception(ExecuteContext ctx) {
        SQLException ex = ctx.sqlException();
        if (ex == null) {
            LOG.error(format(
                "The Spring SQL Exception translator is being passed a context with no SQLException.  Context - runtime exception?: %s",
                ctx.exception()));
        } else {
            DataAccessException translatedEx = translator.translate("jooq", ctx.sql(), ex);
            ctx.exception(translatedEx);
        }
    }

}