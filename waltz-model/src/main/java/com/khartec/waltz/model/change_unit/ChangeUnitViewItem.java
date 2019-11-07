package com.khartec.waltz.model.change_unit;

import com.khartec.waltz.model.assessment_rating.AssessmentRatingDetail;

import java.util.Set;

public abstract class ChangeUnitViewItem {

    public abstract ChangeUnit changeUnit();
    public abstract Set<AssessmentRatingDetail> assessments();

}
