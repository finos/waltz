<script>
    import {buildHierarchies} from "../../../common/hierarchy-utils";
    import {mkChunks} from "../../../common/list-utils";
    import _ from "lodash";

    export let taxonomy = [];
    export let link = "";

    const orderAlphabetically = {
        name: "Alphabetical",
        fn: d => d.name
    };

    const orderBySize = {
        name: "Size",
        fn: x => _.size(x.children) * -1
    };

    const orderNaturally = {
        name: "Natural",
        fn: x => x
    };

    const OrderModes = [
        orderAlphabetically,
        orderBySize,
        orderNaturally
    ];

    let orderMode = orderBySize;

    $: roots = _
        .chain(taxonomy)
        .reject(d => d.deprecated)
        .reject(d => d.name.startsWith("_REMOVED_"))
        .reject(d => d.unknown)
        .thru(buildHierarchies)
        //.reject(d => _.isEmpty(d.children))
        .orderBy(orderMode.fn)
        .value();

    $: nodesPerRow = 6;
    $: itemHeight = 14;
    $: headerHeight = 30;

    $: nodeWidth = 900 / nodesPerRow;

    $: chunked = mkChunks(roots, nodesPerRow);

    $: rowHeights = _
        .chain(chunked)
        .map(nodes => _.max(_.map(nodes, n => _.size(n.children))))
        .reduce((acc, h, idx) => {
                acc.values.push({height: h, start: acc.start});
                return Object.assign(
                    acc,
                    {start: acc.start + h, end: acc.start + h});
            },
            {values: [], start: 0, end: 0})
        .thru(d => d.values)
        .value();

    function simplifyName(name) {
        return name
            .replace(/(M|m)anagement/, "Mgmt")
            .replace(/including/, "incl")
            .replace(/(I|i)dentifier/, "Id")
            .replace(/(I|i)nformation/, "Info")
    }

    function calcRowOffset(rowIdx, itemHeight, headerHeight) {
        if (rowIdx === 0) {
            return 0;
        } else {
            const start = rowHeights[rowIdx].start;
            return start * itemHeight + rowIdx * headerHeight;
        }
    }
</script>

<label>
    Nodes per row ({nodesPerRow}
    <input type="range" min="1" max="8" bind:value={nodesPerRow}>
</label>

<label>
    Item Height ({itemHeight})
    <input type="range" min="0" max="20" bind:value={itemHeight}>
</label>

<label>
    Header Height ({headerHeight})
    <input type="range" min="0" max="60" bind:value={headerHeight}>
</label>

<label>
    Ordering
    <select id="orderMode"
            bind:value={orderMode}>

        {#each OrderModes as m}
            <option value={m}>
                {m.name}
            </option>
        {/each}
    </select>
</label>
<svg width="100%"
     viewBox="0 0 1000 800">


    <style>
        .group-title {
            padding: 0.1em;
            font-size: 10px;
            border-bottom: 1px solid #eee;
            background-color: #f3f3f3;
        }

        .l2-list {
            padding-top: 0.3em;
            padding-left: 0.6em;
            font-size: 8px;
            list-style: disc outside;
        }
    </style>

    <g>
        {#each chunked as nodes, i}
            {#each nodes as node, j}
                <g transform={`translate(${j * (nodeWidth + 10)}, ${calcRowOffset(i, itemHeight, headerHeight)})`}>
                    <rect width={nodeWidth}
                          height={rowHeights[i].height * itemHeight + (i > 0 ? headerHeight : 0)}
                          fill="#fafafa"/>
                    <foreignObject width={nodeWidth}
                                   height="100%">
                        <div class="group-title">
                            <a href="data-types/{node.id}">{simplifyName(node.name)}</a>
                        </div>
                        <ul class="l2-list">
                            {#each _.orderBy(node.children, c => c.name) as child}
                                <li>
                                    <a href="{link}/{child.id}">{simplifyName(child.name)}</a>
                                </li>
                            {/each}
                        </ul>
                    </foreignObject>

                </g>
            {/each}
        {/each}
    </g>
</svg>
