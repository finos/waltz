package org.finos.waltz.jobs.example;

public enum InvestmentRating {
        INVEST("Invest", "I", "green"),
        DISINVEST("Disinvest", "D", "red"),
        MAINTAIN("Maintain", "M", "orange"),
        NOT_APPLICABLE("N/A", "Z", "grey");


        final String displayName;
        final String code;
        final String color;


        InvestmentRating(String displayName, String code, String color) {
            this.displayName = displayName;
            this.code = code;
            this.color = color;
        }
    }