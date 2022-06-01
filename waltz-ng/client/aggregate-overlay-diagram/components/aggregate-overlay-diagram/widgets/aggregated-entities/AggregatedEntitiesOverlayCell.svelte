<script>
    import EntityLabel from "../../../../../common/svelte/EntityLabel.svelte";
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import {scaleLinear} from "d3-scale";
    import _ from "lodash";

    export let cellData = {};
    export let maxCount;

    $: r = scaleLinear()
        .domain([0, maxCount])
        .range([0, 18]);


    $: cr = r(references.length)

    let references = [];

    $: references = cellData?.aggregatedEntityReferences || [];

    $: height = references.length === 0
        ? 28
        : (references.length + additionalLines) * 20 + 40;

    $: additionalLines = _
        .chain(references)
        .map(r => r.name)
        .map(name => _.floor(_.size(name) / 47))
        .sum()
        .value();

</script>


<svg class="content"
     width="100%"
     {height}
     style="background: white">
    {#if _.size(references) == 0}
        <text font-size="16"
              dy="26"
              dx="60">
            -
        </text>
    {:else}
        <circle r={cr}
                fill="#a9e4ff"
                stroke="#25b0ff"
                stroke-width="2"
                cx="30"
                cy="20"/>

        <!--        Count label-->

        <text font-size="16"
              dy="26"
              dx="60">
            {_.size(references)}
        </text>

        <!--        List of entities-->
        <foreignObject transform="translate(0, 40)"
                       width="300"
                       height="100%">
            <ul>
                {#each references as ref}
                    <li>
                        <EntityLabel {ref}/>
                        <EntityLink {ref}>
                            <Icon name="hand-o-right"/>
                        </EntityLink>
                    </li>
                {/each}
            </ul>
        </foreignObject>
    {/if}
</svg>


<style>


    ul {
        padding: 0.1em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
        font-size: 12px
    }

    li {
        padding-top: 0.1em;
    }

</style>
