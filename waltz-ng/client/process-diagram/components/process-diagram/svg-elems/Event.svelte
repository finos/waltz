<script>
    import {lookupSubTypeComponent} from "../process-diagram-utils";

    export let obj;
    export let layout;
    export let isSelected;

    $: subTypeComponent = lookupSubTypeComponent(obj.objectSubType);


</script>


<g class={obj.stereotype} >

    <g transform="translate({layout.width / 2} 0)">

        <circle r={layout.width / 1.8}
                class="outer {isSelected ? 'selected':''}">
        </circle>

        {#if obj.stereotype !== 'StartEvent'}
            <circle r={layout.width / 2.2}
                    class="inner">
            </circle>
        {/if}
    </g>

    <!-- subtype -->
    {#if subTypeComponent}

        <g transform="translate(0 {layout.height / 2 * -1})"
           class="subtype">
            <svelte:component this={subTypeComponent}
                              width={layout.width}
                              height={layout.height}/>
        </g>
    {/if}


    <foreignObject width={layout.width * 4}
                   height={layout.height}
                   border="1px solid red"
                   transform={`translate(${layout.width * -1.4}, ${layout.height * 0.5})`}>
        <div>
            {obj.name}
        </div>
    </foreignObject>
</g>


<style>
    .subtype {
        stroke: #aaa;
    }
    circle {
        stroke: #aaa;
    }

    foreignObject div {
        text-align: center;
        font-size: 10px;
    }

    .EndEvent .inner {
        fill: #fff4f4;
    }

    .EndEvent .outer {
        fill: #ff8686;
    }

    .IntermediateEvent {
        fill: #ffffe0;
    }

    .StartEvent {
        fill: #e1ffe0;
    }

    .outer.selected {
        opacity: 1;
        stroke: #2b98ff;
        stroke-width: 3;
    }

</style>