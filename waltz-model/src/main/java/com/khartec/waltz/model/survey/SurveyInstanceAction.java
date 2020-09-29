package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SurveyInstanceAction {

    SUBMITTING("Submit", false),
    REJECTING("Reject", true),
    WITHDRAWING("Withdraw", false),
    REOPENING("Reopen", false),
    SAVING("Save", false),
    APPROVING("Approve", true);

    private final String display;
    private final boolean isCommentMandatory;

    SurveyInstanceAction(String display, boolean isCommentMandatory) {
        this.display = display;
        this.isCommentMandatory = isCommentMandatory;
    }

    public String getDisplay() {
        return display;
    }

    public String getName() {
        return name();
    }

    public boolean isCommentMandatory() {
        return isCommentMandatory;
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