export function updateUrlWithoutReload($state, navItem) {
    $state.go('.', {id: navItem.id}, {notify: false});
}


const defaultDefinitions = {
    children: [],
    parent: null,
    siblings: []
};


export function hasRelatedDefinitions(definitions = defaultDefinitions) {
    const relatedCount = definitions.children.length
        + (definitions.parent
            ? 1
            : 0)
        + definitions.siblings.length;

    return relatedCount > 0;
}



