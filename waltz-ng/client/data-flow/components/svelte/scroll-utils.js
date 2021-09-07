export const dimensions = {
    client: {
        height: 20,
        width: 200
    },
    category: {
        height: 40,
        width: 200
    },
    clientList: {
        paddingTop: 20,
        innerPadding: 1.4
    },
    diagram: {
        height: 500,
        width: 700
    }
};

const catLayout = {
    id: a => a.categoryId,
    scale: (catYPos, cliYPos) => catYPos,
    dimensions: dimensions.category,
    offset: () => 0
}

const cliLayout = {
    id: a => a.clientId,
    scale: (catYPos, cliYPos) => cliYPos,
    dimensions: dimensions.client,
    offset: (x) => x
}

export const layout = {
    categoryToClient: {
        left: catLayout,
        right: cliLayout,
        clientTranslateX: dimensions.diagram.width - dimensions.client.width,
        categoryTranslateX: 0
    },
    clientToCategory: {
        left: cliLayout,
        right: catLayout,
        clientTranslateX: 0,
        categoryTranslateX: dimensions.diagram.width - dimensions.category.width
    }
}


export const activeLayout = layout.clientToCategory;


export function randomPick(xs) {
    if (!xs) throw new Error("Cannot pick from a null set of options");

    const choiceCount = xs.length - 1;
    const idx = Math.round(Math.random() * choiceCount);
    return xs[idx];
}