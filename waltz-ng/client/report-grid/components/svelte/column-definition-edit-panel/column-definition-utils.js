
export const derivedColumnHelpText = `
* \`< <= > >= == != && || ! \`: logical operators
* \`anyCellsProvided(cellExtId, ...)  allCellsProvided(...) ratioProvided(....) percentageProvided(....)\`: cell checks
* \`mkResult(value, <optionText>, <optionCode>)\`: makes a result cell
* Example:
    * \`allCellsProvided('CRITICALITY', 'INVEST')
    ? mkResult("y")
    : mkResult("n");\`

See the <a href="https://commons.apache.org/proper/commons-jexl/reference/syntax.html" target="_blank">JEXL documentation</a> for more information.
`;