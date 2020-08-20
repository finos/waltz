package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SurveyInstanceAction {

    SUBMITTING("Submit"),
    REJECTING("Reject"),
    WITHDRAWING("Withdraw"),
    REOPENING("Reopen"),
    SAVING("Save"),
    APPROVING("Approve");

    private final String display;

    SurveyInstanceAction(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public String getName() {
        return name();
    }

    public static SurveyInstanceAction findByDisplay(String display) {
        for (SurveyInstanceAction action: SurveyInstanceAction.values()) {
            if (action.display.equalsIgnoreCase(display)) {
                return action;
            }
        }
        return null;
    }
}
