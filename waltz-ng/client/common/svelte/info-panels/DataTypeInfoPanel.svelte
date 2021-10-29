
<script>

    import EntityLink from "../EntityLink.svelte";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";
    import DescriptionFade from "../DescriptionFade.svelte";
    import {dataTypeStore} from "../../../svelte-stores/data-type-store";

    export let primaryEntityRef;

    $: dataTypeCall = dataTypeStore.getById(primaryEntityRef.id);
    $: dataType = $dataTypeCall.data;

</script>

{#if dataType}
    <h4><EntityLink ref={dataType}/></h4>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">External Id</td>
                <td width="50%">{dataType.code}</td>
            </tr>
        </tbody>
    </table>

    <DescriptionFade text={dataType.description}/>
    <br>

    <slot name="post-header"/>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>

    <slot name="footer"/>
{/if}