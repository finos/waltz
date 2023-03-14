### Bulk Loader File

Column headings are fixed, assessment rating columns can take two forms, example below. Multiple distinct assessment
columns are allowed:

| Column Name                            | Is Mandatory | Description                                                                                                                                                                                    | Example   |
|----------------------------------------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| Entity External Id                     | Y            | External identifier of the entity to be linked e.g Application asset code                                                                                                                      | 12345-1   |
| Legal Entity External Id               | Y            | External Identifier of the legal entity to be linked                                                                                                                                           | ABCD      |
| Comment                                | N            | Additional comment to add to the relationship. When updating, empty comments are ignored                                                                                                       | -         |
| Remove Relationship                    | N            | Column to indicate whether this relationship should be deleted. Takes values 'Y' or 'X'                                                                                                        | Y         | 
| < ASSESSMENT_DEFINITION >              | N            | Can be used to populate update assessment ratings for the relationship. Supports multi valued assessments separated by ';', values are the external id, code or name of the rating scheme item | A;B       |
| < ASSESSMENT_DEFINITION > / < RATING > | N            | Can be used to populate update assessment ratings for the relationship. Supports comments in cell, or to add rating without comment use one of 'Y' or 'X'                                      | Y         |

TODO: Upload Modes

We need to this differentiate beteen the style of update to perform. Add only will preserve comments and assessments not
listed. Replace will remove any existing extraneous comments/assessments that are not mentioned in the file.
