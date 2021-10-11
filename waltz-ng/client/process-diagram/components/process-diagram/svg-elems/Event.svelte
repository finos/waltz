<script>
    import Message from "./sub-types/Message.svelte";
    import Timer from "./sub-types/Timer.svelte";
    export let obj;
    export let layout;

    function onMouseEnter() {
        console.log("event:me:", obj);
    }

    function lookupSubTypeComponent(subType) {
        switch (subType) {
            case "Message":
                return Message;
            case "Timer":
                return Timer;
            default:
                return null;
        }
    }

    $: subTypeComponent = lookupSubTypeComponent(obj.objectSubType);
</script>


<g on:mouseenter={onMouseEnter}
   class={obj.stereotype}>


    <g transform="translate({layout.width / 2} 0)">

        <circle r={layout.width / 1.8}
                class="outer">
        </circle>

        {#if obj.stereotype !== 'StartEvent'}
            <circle r={layout.width / 2.2}
                    class="inner">
            </circle>
        {/if}
    </g>

    <!-- subtype -->
    {#if subTypeComponent}
        <clipPath id="myClip_{obj.objectId}">
            <!--
              Everything outside the circle will be
              clipped and therefore invisible.
            -->
            <circle transform="translate({layout.width / 2} {layout.height / 2})"
                    r={layout.width / 1.8}></circle>
        </clipPath>

        <g transform="translate(0 {layout.height / 2 * -1})"
           class="subtype"
           clip-path="url(#myClip_{obj.objectId})">
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

</style>