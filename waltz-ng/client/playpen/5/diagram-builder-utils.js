export const FlexDirections = {
    COLUMN: "column",
    ROW: "row"
}

export const DefaultProps = {
    itemHeight: 5,
    itemWidth: 10,
    flexDirection: FlexDirections.ROW,
    showTitle: true,
    bucketSize: 3,
    proportion: 1
}

export const DiagramKinds = {
    GROUP: "GROUP",
    ITEM: "ITEM"
}

export const ControlModes = {
    VIEW: "VIEW",
    EDIT_GROUP: "EDIT_GROUP",
    EDIT_ITEMS: "EDIT_ITEMS",
}


export function mkGroup(name, id, parentId, position, data = null) {

    return {
        title: name,
        id,
        parentId: parentId,
        props: DefaultProps,
        kind: DiagramKinds.GROUP,
        itemKind: null,
        position,
        data
    }
}

export function mkItem(name, id, groupId, data = null) {

    return {
        title: name,
        id,
        groupId,
        kind: DiagramKinds.ITEM,
        data
    }
}

