---
layout: post
title:  "Tutorial: Creating Conditional Surveys"
date:   2020-09-17
categories: waltz 
---

# Tutorial: Creating Conditional Surveys
In this tutorial we will look at the new _Conditional Survey_ feature introduced as part of [Waltz 1.27](https://github.com/finos/waltz/releases/tag/1.27.2).


## Predicates
Questions in conditional surveys may have predicates associated to them. 
These predicates can broadly be categorized as either _response-based_ or _subject-based_.

**Response-Based** predicates use data from other responses in the survey to determine if a question should be shown.
A simple example is: `isChecked("IN_SCOPE", true)` which would return a boolean based on whether the response to the question with external id: `IN_SCOPE` is checked.
If it has not yet been answered assume it is in scope (the second argument provided this default value of`true`)

**Subject-Based** predicates use data from the Waltz entity being surveyed. 
So an application survey would have the corresponding application as it's subject, a change initiative would have the corresponding entity.
A simple example is: `isRetiring()` which would return `true` if the app is scheduled for retirement or `false` if the app is not scheduled for retirement (or has already retired).

Note, some functions are not strictly predicates; they return numeric or string results.
These need to be converted into predicates by using operators such as `>` or `==`. 
For example, `assessmentRating('DATA_SENSITIVITY') == 'HIGH'`.

The current list of functions that can be used is given at the end of this tutorial.


### Combining Predicates
Sometimes a simple predicate is not sufficient to express a rule.
Waltz allows for predicates to be combined using simple boolean operators.
An example: 
`isChecked('IN_SCOPE', true) && isRetiring()` 
which would only show the associated question if the `IN_SCOPE` checkbox was ticked (or not yet answered) **and** the application being surveyed is scheduled for retirement.

More complex predicates can be built using parentheses for precedence, e.g. 
`A || (B && C) || !D`     


### Reusing Predicates
As surveys increase in complexity we may find ourselves repeating complex predicates for multiple questions.
There is a helper function, `ditto`, which is useful in this scenario.
`ditto` allows you to reuse a predicate from another question and optionally build upon it by combining with additional predicates.

For example, take a question (`Q1`) with a predicate: 
`isChecked('IN_SCOPE', true) && isRetiring()` 
and another question (`Q2`) with a predicate like: 
`ditto('Q1') && numberVal('ESTIMATE') > 10`. 
This effectively expands to make the `Q2` predicate equivalent to:
`(isChecked('IN_SCOPE', true) && isRetiring()) && numberVal('ESTIMATE') > 10`  


### Best Practices
As can be seen in the above examples, having well labelled external identifiers for questions helps make the predicate logic readable.
Use `ditto` to increase readability and to minimise rework if a common base predicate changes.


## Function Reference
* `< <= > >= == != && || ! `: logical operators
* `isChecked(extId, <defaultValue>)`: `true` if the question with the given ext id is checked, `false` if not checked,
  or \`defaultValue\` if the answer is currently undefined.
* `numberValue(extId, <defaultValue>)`: numeric value of the response for the given ext id (or `defaultValue`)
* `ditto(extId)\`: evaluates same conditions from a different question.  Useful for repetition of complex predicates.
* `val(extId, <defaultValue>)`: returns the current value
* `assessmentRating(name|extId, <defaultValue>)`: returns code value of the matching rating (returns null if no default given and no assessment found)
* `belongsToOrgUnit(name|extId)`: returns true if the subject app is part of the given org unit tree
* `dataTypeUsages(name|extId)`: returns set of usage kinds for the given data types (use the `=~` operator to test for membership)
* `isRetiring()`: (application only) true if app has planned retirement date but no actual retirement date
* `hasDataType(name|extId)`: returns whether the specified datatype (or a descendent) is in use by the app

## Full Example
Let's look at an application based survey to see a complete example:

![Survey Definition](/blog/assets/images/surveys/example.png)

Here we can see the survey definition for a simple survey which is gathering data for a cloud migration programme.
The first question toggles the all the other questions, with q2 being shown if the app is out of scope 
(note the negation operator, `!` in `! isChecked('IN_SCOPE')`).
The remaining questions build upon each other using `ditto` and the end result is a dynamic form which can manifest itself in any of the following ways:

![Survey Definition](/blog/assets/images/surveys/results.png)


## Conclusion
Conditional surveys greatly increase the ability of surveys to capture the data you want without swamping the recipient in irrelevant details.

We are looking to expand the feature with new predicates and capabilities over the coming releases. 
If you have any suggestions please contact us via our [Github Issues](https://github.com/finos/waltz/issues) pages.




----

You may also be interested in checking out the complete [Waltz playlist](https://www.youtube.com/playlist?list=PLGNSioXgrIEfJFJCTFGxKzfoDmxwPEap4) on YouTube.