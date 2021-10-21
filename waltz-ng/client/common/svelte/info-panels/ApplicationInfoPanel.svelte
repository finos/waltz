
<script>

    import {applicationStore} from "../../../svelte-stores/application-store";
    import EntityLink from "../EntityLink.svelte";
    import {lifecyclePhase} from "../../services/enums/lifecycle-phase";
    import {criticality} from "../../services/enums/criticality";
    import _ from "lodash";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";
    import DescriptionFade from "../DescriptionFade.svelte";

    export let primaryEntityRef;

    $: appCall = applicationStore.getById(primaryEntityRef.id);
    $: app = $appCall.data;

</script>

{#if app}
    <h4><EntityLink ref={app}/></h4>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">Asset Code</td>
                <td width="50%">{app.assetCode}</td>
            </tr>
            <tr>
                <td width="50%">Kind</td>
                <td width="50%">{app.applicationKind}</td>
            </tr>
            <tr>
                <td width="50%">Lifecycle</td>
                <td width="50%">{_.get(lifecyclePhase[app.lifecyclePhase], "name", app.lifecyclePhase)}</td>
            </tr>
            <tr>
                <td width="50%">Criticality</td>
                <td width="50%">{_.get(criticality[app.businessCriticality], "name", app.businessCriticality)}</td>
            </tr>
        </tbody>
    </table>

    <DescriptionFade text={app.description}/>
    <br>

    <slot name="post-header"/>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>

    <slot name="footer"/>
{/if}