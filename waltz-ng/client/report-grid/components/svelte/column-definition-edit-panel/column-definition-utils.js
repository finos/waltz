
export const derivedColumnHelpText = `
* Logical operators: \`< <= > >= == != && || ! \`
* Cell checks: \`anyCellsProvided(cellExtId, ...)\`  \`allCellsProvided(...)\`  \`ratioProvided(....)\`  \`percentageProvided(....)\`
* Make a result cell: \`mkResult(value, <optionText>, <optionCode>)\`
* Example: check cell data provided
    * \`allCellsProvided('CRITICALITY', 'INVEST')
    ? mkResult("y")
    : mkResult("n");\`
* Example: evaluate cell text
    * \`cell('CRITICALITY').textValue() == 'Critical'
    &&
    cell('INVEST').textValue() == 'Invest'
       ? mkResult("IMPORTANT")
       : null;\`

See the <a href="https://commons.apache.org/proper/commons-jexl/reference/syntax.html" target="_blank">JEXL documentation</a> for more information.
`;