<script>
    import {selectedAnnotation, selectedFlow, selectedNode, store} from "../diagram-model-store";
    import NodePanel from "./NodePanel.svelte";
    import DefaultPanel from "./DefaultPanel.svelte";
    import FlowPanel from "./FlowPanel.svelte";
    import LastEdited from "../../../../common/svelte/LastEdited.svelte";
    import {flowDiagramStore} from "../../../../svelte-stores/flow-diagram-store";
    import AnnotationPanel from "./AnnotationPanel.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import OverlayGroupsPanel from "./OverlayGroupsPanel.svelte";
    import dirty from "../store/dirty";


    export let doSave;
    export let diagramId;
    let selectedApp = null;

    function cancel() {
        $selectedNode = null;
        $selectedFlow = null;
        $selectedAnnotation = null;
    }

    $: diagramCall = flowDiagramStore.getById(diagramId);
    $: diagram = $diagramCall.data;


</script>

<div style="padding-bottom: 1px">
    <h4>{$store.model?.title}</h4>
    <p class="help-block">{$store.model?.description || "No description provided"}</p>
    <div class="small pull-right text-muted">
        (<LastEdited class="small pull-right text-muted" entity={diagram}/>)
    </div>
</div>

<hr>

{#if $selectedNode}
    <NodePanel selected={$selectedNode} on:cancel={cancel}/>
{:else if $selectedFlow}
    <FlowPanel selected="{$selectedFlow}" on:cancel={cancel}/>
{:else if $selectedAnnotation}
    <AnnotationPanel selected="{$selectedAnnotation}" on:cancel={cancel}/>
{:else}
    <p class="help-block">Select a node or flow on the diagram to make changes</p>
    <DefaultPanel {doSave}/>
{/if}

<hr>

<OverlayGroupsPanel {diagramId}/>


{#if $dirty}
    <span class="help-block">
        <Icon name="exclamation-circle"/>
        Changes have been made to this diagram, if you do not
        <button class="btn btn-skinny"
                on:click={() => doSave()}>save
        </button>
        them they will be lost.
    </span>
{/if}

<style>
</style>