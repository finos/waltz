# Waltz 1.2 and beyond

## Executive Summary

The next two releases of Waltz will be focused on improving its capabilities to accurately reflect the technical landscape of an organisation.  To do this we need to be able to rate applications against multiple measurables (location, product, function, etc.) and show some of the nuances between these measurables.  

Waltz will also evolve to capture proposed changes to the technical landscape.  These changes will be expressed as deltas between the current and future states of the organisation.  To give context to these deltas, it will be possible to associate them to change initiatives (programmes and projects).


## Q1 - Main Theme: Measurables & Perspectives

### Measurables

Currently (v1.1) Waltz supports rating an application at an overall level and/or against each function that the application performs.  It is proposed to generalise this feature by introducing **measurables**.  Measurables are a set of elements against which an application can be rated.  These are:

- Function
- Product
- Region
- Business Line
- Process
- Service
  
_It is not proposed to make this list open-ended as we wish to promote standardisation of the underlying meta-model. An open ended meta model would significantly increase complexity for users and developers for a limited gain in value._ 

For example, an Application may be rated against regions as follows:

Region|Rating
------|------
Canada|R
Germany|G
UK|G
US|R

...and products:

Product|Rating
------|-----
Mortgages|G
Credit|A
Current|G

From a glance we can see the application deals with mortgages, credit and current accounts.  It is strategic (**G**reen) for mortgages and current accounts, less strategic (**A**mber) for credit accounts.

It also supports North America (non-strategic, **R**ed) and also the UK with Germany (strategic).  

However, using the above is not sufficient to address more complex scenarios - for examples how well the application supports mortgages in Canada.


### Perspectives

To support a more complex view of the landscape, Waltz will introduce the concept of perspectives.  A perspective is formed by associating two measures  and rating applications against the permutations of those measures.  For example a `Product - Region` perspective may look like this for a given application:

             
 Product -> | Mortgage  | Credit  | Current 
 ---------- | --------- | ------- | -------- 
 **Region** | 
 Canada     |    R      |    R       |    -    
 Germany    |    G      |    G       |    G    
 UK         |    G      |    A       |    G    
 US         |    R      |    G       |    -    
 
We can now see a more nuanced view of the product and regions that this application supports.   North America does not support current accounts, Germany is strategic for current accounts and the `US` is strategic for `Credit` accounts.  This last combination should probably be flagged as it contradicts both general ratings (`US` was `R`, and `Credit` was `A`).

Multiple perspectives can be created for each application to cover additional combinations of measurables.  These perspectives will be centrally managed by the organisation to promote consistency when describing their estate.

So far we have viewed applications as the focal point.  However Waltz will allow the focal point to be any of the perspectives three constituent parts - measurable 1, measurable 2 and the associated application.  

Viewing from different focal points is analogous to pivoting the data around that point.  For example if we were to change our focal point to be Germany (via the `Region...Germany` page) we would see a table rendering applications against products, as shown below:

        | Mortgage | Credit | Current | Cash 
------- | -------- | ------ | ------- | -------  
 App A  |    G     |    G   |    G    |  -
 App B  |    -     |    -   |    A    |  G
 

### Inter-Measurable Relationships 

Measurables should be linkable to other measurables to allow for quick navigation/exploration/filtering of related concepts.  These relationships may be thought of similarly to traits - something that binds disparate elements .  For example we may wish to relate Investment Banking as a business line to the product,  processes or other measurables that it supports.


### Future Enhancements

It is anticipated that, subject to demand, related future work will include:

- Associating perspectives to organisational units.  This will allow parts of the organisation to mandate (and track the completion of) a fixed set of perspectives.
- Deriving Inter-Measurable Relationships from perspective data.  Waltz could provide analysis to show which groups of measurables show a high degree of correlation.
- Using these relationships as an additional grouping mechanism for viewing collections of applications.



## Q2 - Main Theme: Roadmaps as Deltas

To show how an organisation's technical landscape changes over time we need to capture information about how the current (**baseline**) landscape is modified over time.  

### Deltas 

Changes to the baseline will be captured as deltas to the baseline and will initially focus on the following items (in priority order):

- application status (retirements / promotion)
- measurables
- perspective ratings
- flow changes

We will not consider changes to items such as servers, databases, involvement etc. as Waltz is not aiming to be a golden source for these items.

Each delta will be associated with a target date (year/quarter) and may optionally be linked to change initiatives to provide context and grouping. All deltas will be expressed as changes to the baseline, chains of deltas will _not_ be supported as the user interaction and implementation complexity will be prohibitive.



## Additional Items  

The following are significant enhancements that will be addressed in priority order as time allows:


### Physical Flow data types

We currently allow data types to be associated to logical flows but not physical flows.  This modification will extend the physical flow information to include data types.  Data types between logical and physical flows will be synchronised to ensure they remain consistent.


### Surveys & Attestation

Waltz is well-positioned to accommodate ad-hoc data capture.  Questionnaires can be prepared and targeted to the relevant people associated to the applications in the survey scope.  

The responses can be stored and progress to completion by the target group can be tracked.  By adopting an open approach to ad-hoc data capture we hope to reduce the effort and increase the exposure of collected data sets.  

Attestation can be viewed as a specialised case of a survey and therefore the same tooling will be used.


### Cleanup tasks

Waltz can detect certain inconsistencies in data.  Examples will include:

- Retired or Conceptual applications with logical/physical flows
- In-house applications with no source code repository 

Waltz will be able to flag these cases and provide guidance to end users on how to resolve them (e.g. delete flows / amend lifecycle phase ,  add scm bookmark / amend application type).  Quantity and severity of cleanup tasks will be tracked and reported to provide guidance to show areas where attention is required.


### Other 

The **search** facilities within Waltz are to be enhanced, we will look to replace the current search with a pluggable layer so that customers may opt to use a more fully featured search engine such as Elastic Search.

Creating navigational aids, **SVG diagrams** linked to Waltz entities, will be improved.  We intend to investigate ways to make this process simpler - either via a simplified editor or by a simple way to perform the entity linkage to existing diagram elements.  This can then be further extended into a general purpose diagram solution.

**Insights**, as Waltz collates more information we are able to generated custom reports drawing upon multiple data sources.  One example is to identify segregation of duty concerns by combining user, application and function data. 


