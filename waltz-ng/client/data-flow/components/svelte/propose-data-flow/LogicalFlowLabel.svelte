<script>
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {sameRef} from "../../../../common/entity-utils";

    export let logicalFlow;
    export let primaryEntityRef;
</script>

<span>
    <span class:text-muted={sameRef(logicalFlow.source, primaryEntityRef)}>
        <EntityLabel ref={logicalFlow.source}/>
    </span>

    <span class="flow-arrow">
        <Icon name="arrow-right"/>
    </span>

    <span class:text-muted={sameRef(logicalFlow.target, primaryEntityRef)}>
        <EntityLabel ref={logicalFlow.target}/>
    </span>
</span>
