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
            "Paris",
            "Manchester",
            "Croydon",
            "Dallas",
            "Redmond",
            "Austin",
            "Frankfurt",
            "Turin",
            "Madrid",
            "Oslo",
            "Moscow",
            "Bucharest",
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


    String[] operatingSystemVersions = new String[] {
            "1.0",
            "1.1",
            "2.0",
            "3.0",
            "2000",
            "XP",
            "ME",
            "Vista"
    };
}
