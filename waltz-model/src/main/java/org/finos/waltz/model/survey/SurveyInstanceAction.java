package org.finos.waltz.model.survey;

import com.fasterxml.jackson.annotation.JsonFormat;

import static org.finos.waltz.model.survey.SurveyInstanceActionAvailability.*;
import static org.finos.waltz.model.survey.SurveyInstanceActionCompletionRequirement.ALLOW_PARTIAL_COMPLETION;
import static org.finos.waltz.model.survey.SurveyInstanceActionCompletionRequirement.REQUIRE_FULL_COMPLETION;
import static org.finos.waltz.model.survey.SurveyInstanceActionConfirmationRequirement.*;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SurveyInstanceAction {

    SUBMITTING("Submit", "submitted", "cloud-upload", "success", CONFIRM_REQUIRED, EDIT_AND_VIEW, REQUIRE_FULL_COMPLETION),
    REJECTING("Reject", "rejected", "ban", "danger", CONFIRM_AND_COMMENT_REQUIRED, VIEW_ONLY, ALLOW_PARTIAL_COMPLETION),
    WITHDRAWING("Withdraw", "withdrawn", "trash-o", "danger", CONFIRM_REQUIRED, VIEW_ONLY, ALLOW_PARTIAL_COMPLETION),
    REOPENING("Reopen", "reopened", "undo", "warning", NOT_REQUIRED, VIEW_ONLY, ALLOW_PARTIAL_COMPLETION),
    SAVING("Save", "saved", "floppy-o", "info", NOT_REQUIRED, EDIT_ONLY, ALLOW_PARTIAL_COMPLETION),
    APPROVING("Approve", "approved", "check-square-o", "success", CONFIRM_AND_COMMENT_REQUIRED, VIEW_ONLY, REQUIRE_FULL_COMPLETION);


    private final String display;
    private final String verb;
    private final String icon;
    private final String style;
    private final SurveyInstanceActionConfirmationRequirement confirmationRequirement;
    private final SurveyInstanceActionAvailability availability;
    private final SurveyInstanceActionCompletionRequirement completionRequirement;


    SurveyInstanceAction(String display,
                         String verb,
                         String icon,
                         String style,
                         SurveyInstanceActionConfirmationRequirement confirmationRequirement,
                         SurveyInstanceActionAvailability availability,
                         SurveyInstanceActionCompletionRequirement completionRequirement) {
        this.display = display;
        this.verb = verb;
        this.icon = icon;
        this.style = style;
        this.confirmationRequirement = confirmationRequirement;
        this.availability = availability;
        this.completionRequirement = completionRequirement;
    }


    public String getDisplay() {
        return display;
    }


    public String getName() {
        return name();
    }



    public SurveyInstanceActionAvailability getAvailability() {
        return availability;
    }


    public SurveyInstanceActionConfirmationRequirement getConfirmationRequirement() {
        return confirmationRequirement;
    }


    public static SurveyInstanceAction findByDisplay(String display) {
        for (SurveyInstanceAction action: SurveyInstanceAction.values()) {
            if (action.display.equalsIgnoreCase(display)) {
                return action;
            }
        }
        return null;
    }


    public SurveyInstanceActionCompletionRequirement getCompletionRequirement() {
        return completionRequirement;
    }


    public String getVerb() {
        return verb;
    }


    public String getIcon() {
        return icon;
    }


    public String getStyle() {
        return style;
    }

}