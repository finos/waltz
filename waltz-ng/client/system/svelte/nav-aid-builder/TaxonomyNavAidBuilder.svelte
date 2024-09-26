<script>
    import {buildHierarchies} from "../../../common/hierarchy-utils";
    import {mkChunks} from "../../../common/list-utils";
    import _ from "lodash";
    import ColorPicker from "../ratings-schemes/ColorPicker.svelte";

    export let taxonomy = [];
    export let link = "";

    const orderAlphabetically = {
        name: "Alphabetical",
        fn: d => d.name
    };

    const orderBySize = {
        name: "Size",
        fn: d => _.size(d.children) * -1
    };

    const orderNaturally = {
        name: "Natural",
        fn: d => d
    };

    const orderByPosition = {
        name: "By Position (then name)",
        fn: d => d.position + d.name
    };

    const OrderModes = [
        orderAlphabetically,
        orderBySize,
        orderNaturally,
        orderByPosition
    ];

    let orderMode = orderByPosition;
    let defaultHeaderColor = "#eeeeee";
    let defaultListColor = "#fafafa";
    let headerColor = "#eee";
    let listColor = "#fafafa";

    $: roots = _
        .chain(taxonomy)
        .reject(d => d.deprecated)
        .reject(d => d.name.startsWith("_REMOVED_"))
        .reject(d => _.includes(_.toLower(d.name), "deprecated"))
        .reject(d => d.unknown)
        .thru(buildHierarchies)
        //.reject(d => _.isEmpty(d.children))
        .orderBy(orderMode.fn)
        .value();

    function simplifyName(name) {
        return name
            .replace(/(M|m)anagement/, "Mgmt")
            .replace(/including/, "incl")
            .replace(/(I|i)dentifier/, "Id")
            .replace(/(I|i)nformation/, "Info")
    }

</script>

<div>
    <b>Header Color ({headerColor}):</b>
    <ColorPicker startColor={defaultHeaderColor}
                 on:select={c => headerColor = c.detail}/>
</div>

<div>
    <b>List Color ({listColor}):</b>
    <ColorPicker startColor={defaultListColor}
                 on:select={c => listColor = c.detail}/>
</div>

<div>
    <b style="display: block">Ordering:</b>
    <select bind:value={orderMode}>

        {#each OrderModes as m}
            <option value={m}>
                {m.name}
            </option>
        {/each}
    </select>
</div>

<hr>

<h3>Output</h3>
<div class="help-block">
    Inspect the diagram below using your browser tools, then cut n' paste into the appropriate
    <span style="font-family: monospace">svg_diagram</span> row.  The diagram element has the
    <span style="font-family: monospace">rendered-navaid</span> class associated with it.
</div>

<div class="rendered-navaid">
    <!-- Header Color: {headerColor},  List Color: {listColor} -->
    <style>
        .container {
            width: 100%;
            display: flex;
            flex-direction: row;
            flex-wrap: wrap;
        }
        .group {
            width: 10em;
            border: 1px solid #ddd;
            margin: 0.5em;
        }

        .group-name {
            padding: 0.2em;
        }

        .l2-list-item {
            padding-top: 0.3em;
            padding-left: 0.6em;
            font-size: 8px;
            list-style: none;
        }
    </style>

    <div class="container">
        {#each roots as root}
            <div class="group" style={`background-color: ${listColor}`}>
                <div class="group-name" style={`background-color: ${headerColor}`}>
                    <a href="{link}/{root.id}" title={root.description}>{simplifyName(root.name)}</a>
                </div>
                <ul class="l2-list-item">
                    {#each _.orderBy(root.children, c => c.name) as child}
                        <li title={root.description}>
                            <a href="{link}/{child.id}">{simplifyName(child.name)}</a>
                        </li>
                    {/each}
                </ul>
            </div>
        {/each}
    </div>
</div>

