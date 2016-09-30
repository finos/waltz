export function updateUrlWithoutReload($state, navItem) {
    $state.go('.', {id: navItem.id}, {notify: false});
}


const defaultDefinitions = {
    children: [],
    parent: null
};


export function hasRelatedDefinitions(definitions = defaultDefinitions) {
    return definitions.children.length > 0;
}



