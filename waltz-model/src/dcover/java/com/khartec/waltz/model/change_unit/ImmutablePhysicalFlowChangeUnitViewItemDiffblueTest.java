package com.khartec.waltz.model.change_unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.assessment_rating.AssessmentRatingDetail;
import java.util.HashSet;
import org.junit.Test;

public class ImmutablePhysicalFlowChangeUnitViewItemDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutablePhysicalFlowChangeUnitViewItem.Json()).assessments.size());
  }
  @Test
  public void setAssessmentsTest() {
    // Arrange
    ImmutablePhysicalFlowChangeUnitViewItem.Json json = new ImmutablePhysicalFlowChangeUnitViewItem.Json();
    HashSet<AssessmentRatingDetail> assessmentRatingDetailSet = new HashSet<AssessmentRatingDetail>();
    assessmentRatingDetailSet.add(null);

    // Act
    json.setAssessments(assessmentRatingDetailSet);

    // Assert
    assertSame(assessmentRatingDetailSet, json.assessments);
  }
  @Test
  public void setChangeUnitTest() {
    // Arrange
    ImmutablePhysicalFlowChangeUnitViewItem.Json json = new ImmutablePhysicalFlowChangeUnitViewItem.Json();
    ImmutableChangeUnit.Json json1 = new ImmutableChangeUnit.Json();

    // Act
    json.setChangeUnit(json1);

    // Assert
    assertSame(json1, json.changeUnit);
  }
}

