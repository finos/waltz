<script>

    import {selectedObject} from "../diagram-store";
    import {processDiagramStore} from "../../../../svelte-stores/process-diagram-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import Markdown from "../../../../common/svelte/Markdown.svelte";
    import {toLinkExtId} from "../process-diagram-utils";
    import _ from "lodash";


    let processDiagramCall = null;
    let linkedDiagram = null;

    $: {
        const extId = toLinkExtId($selectedObject);

        if(extId) {
            processDiagramCall = processDiagramStore.getByExternalId(extId);
            linkedDiagram = $processDiagramCall.data;
        } else {
            processDiagramCall = null;
            linkedDiagram = null;
        }
    }

</script>

{#if !_.isNull(linkedDiagram)}
    <h4>
        <EntityLink ref={linkedDiagram}/>
    </h4>
    <Markdown text={linkedDiagram.description}/>
{:else}
    <h4>
        {$selectedObject.name}
    </h4>
{/if}