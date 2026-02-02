export const mkResponseObject = (arc, response = [], dropdownResponse = null) => ({
    entityRef: arc,
    response: response,
    dropdownResponse: dropdownResponse
});

export const parseJSON = (string) => {
    try {
        return JSON.parse(string);
    } catch (e) {
        return undefined;
    }
}