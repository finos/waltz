export function toSurveyName(survey) {
    return survey?.surveyInstance?.name
        || survey?.surveyRun?.name
        || survey?.surveyTemplateRef?.name;
}