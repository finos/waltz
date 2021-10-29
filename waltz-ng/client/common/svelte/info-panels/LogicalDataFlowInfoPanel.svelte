
<script>

    import EntityLink from "../EntityLink.svelte";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";
    import {logicalFlowStore} from "../../../svelte-stores/logical-flow-store";

    export let primaryEntityRef;

    $: logicalFlowCall = logicalFlowStore.getById(primaryEntityRef.id);
    $: flow = $logicalFlowCall.data;
    $: flowRef = Object.assign({}, flow, {name: "Logical Flow"})

</script>

{#if flow}
    <h4><EntityLink ref={flowRef}/></h4>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">Source</td>
                <td width="50%">{flow.source?.name}</td>
            </tr>
            <tr>
                <td width="50%">Target</td>
                <td width="50%">{flow.target?.name}</td>
            </tr>
            <tr>
                <td width="50%">Provenance</td>
                <td width="50%">{flow.provenance}</td>
            </tr>
            <tr>
                <td width="50%">Read Only</td>
                <td width="50%">{flow.isReadOnly ? "Yes" : "No"}</td>
            </tr>
        </tbody>
    </table>

    <slot name="post-header"/>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>

    <slot name="footer"/>
{/if}