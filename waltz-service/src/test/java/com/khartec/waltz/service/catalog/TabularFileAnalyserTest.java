/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.service.catalog;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.catalog.ImmutableParseAnalysis;
import com.khartec.waltz.model.catalog.ParseAnalysis;
import org.junit.Test;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


/**
 * Created by dwatkins on 04/12/2015.
 */
public class TabularFileAnalyserTest {

    @Test
    public void foo() throws IOException {

        char[] delimeters = new char[]{',', '|', '\t', ';', '!'};
        char[] quoteChars = new char[]{'"', '\''};


        List<ParseAnalysis> analysisResults = ListUtilities.newArrayList();


        for (char quoteChar : quoteChars) {
            for (char delimeter : delimeters) {

                InputStreamReader simpleReader = getReader();

                CsvPreference prefs = new CsvPreference.Builder(quoteChar, delimeter, "\n")
                        .ignoreEmptyLines(false)
                        .build();

                CsvListReader csvReader = new CsvListReader(simpleReader, prefs);

                List<String> cells = csvReader.read();

                ImmutableParseAnalysis.Builder parseAnalysisBuilder = ImmutableParseAnalysis.builder()
                        .quoteChar(quoteChar)
                        .delimiterChar(delimeter);


                while (cells != null) {
                    parseAnalysisBuilder.addFieldCounts(cells.size());
                    cells = csvReader.read();
                }

                ParseAnalysis parseAnalysis = parseAnalysisBuilder.build();
                analysisResults.add(parseAnalysis);

            }
        }

        analysisResults
                .forEach(r -> {
                    System.out.println(r.quoteChar()
                            + " "
                            + r.delimiterChar()
                            + " => [ "
                            + r.fieldCounts().size()
                            + " ] "
                            + r.fieldCounts());
                });
    }


    private InputStreamReader getReader() {
        InputStream simpleStream = TabularFileAnalyser.class.getClassLoader().getResourceAsStream("simple.csv");
        return new InputStreamReader(simpleStream);
    }
}
