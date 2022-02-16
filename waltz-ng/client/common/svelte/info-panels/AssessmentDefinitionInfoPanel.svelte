<script>

    import EntityLink from "../EntityLink.svelte";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";
    import DescriptionFade from "../DescriptionFade.svelte";
    import {assessmentDefinitionStore} from "../../../svelte-stores/assessment-definition";

    export let primaryEntityRef;

    let assessmentDefinitionCall;

    $: {
        if (primaryEntityRef) {
            assessmentDefinitionCall = assessmentDefinitionStore.getById(primaryEntityRef.id);
        }
    }

    $: definition = $assessmentDefinitionCall?.data;


</script>

{#if definition}
    <h4>
        <EntityLink ref={definition}/>
    </h4>
    <table class="table table-condensed small">
        <tbody>
        <tr>
            <td width="50%">External Id</td>
            <td width="50%">{definition.externalId}</td>
        </tr>
        <tr>
            <td width="50%">Permitted Role</td>
            <td width="50%">{definition.permittedRole}</td>
        </tr>

        </tbody>
    </table>

    <div class="help-block small">
        <DescriptionFade text={definition.description}/>
    </div>
    <br>

    <slot name="post-header"/>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>

    <slot name="footer"/>
{/if}