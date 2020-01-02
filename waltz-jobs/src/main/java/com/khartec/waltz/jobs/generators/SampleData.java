/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.jobs.generators;

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
