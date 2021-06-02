<script>
    import NodeLayer from "./NodeLayer.svelte";
    import FlowLayer from "./FlowLayer.svelte";
    import AnnotationLayer from "./AnnotationLayer.svelte";
    import {store} from "./diagram-model-store";

    let elem;
</script>

<div class="col-md-9">
    <svg viewBox="0 0 1100 600"
         width="100%"
         bind:this={elem}>
        <g transform={$store.layout?.diagramTransform}>

            <FlowLayer positions={$store.layout?.positions}
                       flows={$store.model?.flows}/>

            <NodeLayer positions={$store.layout?.positions}
                       nodes={$store.model?.nodes}/>

            {#if $store.visibility?.layers.annotations}
                <AnnotationLayer positions={$store.layout?.positions}
                                 annotations={$store.model?.annotations}/>
            {/if}
        </g>
    </svg>
</div>
<div class="col-md-3">
    <h4>Context Menu</h4>
    <h5>Foo App</h5>
    <ul>
        <li>Add upstream</li>
        <li>Add downstream</li>
        <li>Add annotation</li>
        <li>Remove</li>
    </ul>
</div>
<hr>

<style>
    svg {
        border: 2px solid #fafafa;
    }

</style>