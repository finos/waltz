<script>
    export let obj;
    export let layout;

    function onMouseEnter() {
        console.log("event:me:", obj);
    }
</script>


<g on:mouseenter={onMouseEnter}
   class={obj.stereotype}>

    <circle cx={layout.width / 2}
            r={layout.width / 1.8}
            class="outer">
    </circle>

    {#if obj.stereotype !== 'StartEvent'}
        <circle cx={layout.width / 2}
                r={layout.width / 2.2}
                class="inner">
        </circle>
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

</style>