/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.sample;

public interface SampleData {

    String[][] jobTitles = {
        { "CEO" },
        { "CIO", "CFO", "CTO", "COO" },
        { "Vice President", "Executive Director"},
        { "Director", "Senior Analyst", "Senior Architect", "Project Director" },
        { "Developer", "Analyst", "QA", "Manager", "Sales Director", "Customer Rep", "Administrator" }
    };

    String[] departmentNames = {
        "IT", "Support", "Operations"
    };



    String[] serverPrefixes = new String[] {
            "srv",
            "db",
            "mid",
            "cmp",
            "grd"
    };


    String[] serverPostfixes = new String[] {
            "a",
            "b",
            "c",
            "d",
            "e"
    };


    String[] environments = new String[] {
            "PROD",
            "QA",
            "DEV",
            "UAT",
            "PREPROD"
    };


    String[] locations = new String[] {
            "London",
            "London",
            "Berlin",
            "Singapore",
            "Sydney",
            "Tokyo",
            "New York",
            "New York"
    };


    String[] operatingSystems = new String[] {
            "Windows",
            "Windows",
            "Windows",
            "Linux",
            "Linux",
            "Linux",
            "Solaris",
            "Solaris",
            "AIX",
            "AS/400"
    };
}
