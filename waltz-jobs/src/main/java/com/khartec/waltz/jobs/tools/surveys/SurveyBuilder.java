package com.khartec.waltz.jobs.tools.surveys;

import com.khartec.waltz.common.LoggingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurveyBuilder {

    private static final Logger LOG =  LoggerFactory.getLogger(SurveyBuilder.class);


    public static void main(String[] args) {
        LoggingUtilities.configureLogging();
        new SurveyBuilder().go();
    }

    private void go() {

    }
}
