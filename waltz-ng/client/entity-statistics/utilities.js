export function updateUrlWithoutReload($state, navItem) {
    $state.go('.', {id: navItem.id}, {notify: false});
}


const defaultDefinitions = {
    children: [],
    parent: null
};


export function hasRelatedDefinitions(definitions = defaultDefinitions) {
    const relatedCount = definitions.children.length
        + (definitions.parent
            ? 1
            : 0);

    return relatedCount > 0;
}



