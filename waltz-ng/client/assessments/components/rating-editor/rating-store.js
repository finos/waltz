import {derived, writable} from "svelte/store";
import {mkEnrichedAssessmentDefinitions} from "../../assessment-utils";
import _ from "lodash";

export const selectedAssessmentId = writable(null);
export const primaryEntityReference = writable(null);
export const selectedRatingId = writable(null);
export const assessmentRatings = writable([]);
export const assessmentDefinitions = writable([]);
export const ratingSchemes = writable([]);
export const permissions = writable([]);

export const permissionsByRatingId = derived(
    [permissions],
    ([$permissions]) => _.keyBy($permissions, d => d.ratingId));

export const defaultPermission = derived(
    [permissions],
    ([$permissions]) => _.find($permissions, d => d.isDefault));

export const assessments = derived(
    [assessmentRatings, assessmentDefinitions, ratingSchemes],
    ([$assessmentRatings, $assessmentDefinitions, $ratingSchemes]) => {
        if ($assessmentDefinitions, $ratingSchemes, $assessmentRatings) {
            return mkEnrichedAssessmentDefinitions(
                $assessmentDefinitions,
                $ratingSchemes,
                $assessmentRatings);
        } else {
            return [];
        }
    });

export const selectedAssessment = derived(
    [selectedAssessmentId, assessments],
    ([$selectedAssessmentId, $assessments]) => _.find($assessments, d => d.definition.id === $selectedAssessmentId));


export const selectedRating = derived(
    [selectedRatingId, selectedAssessment],
    ([$selectedRatingId, $selectedAssessment]) => {
        return _.find($selectedAssessment?.ratings, d => d.rating.id === $selectedRatingId);
    });