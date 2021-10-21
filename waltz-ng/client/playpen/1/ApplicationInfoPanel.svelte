
<script>

    import {applicationStore} from "../../svelte-stores/application-store";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import {lifecyclePhase} from "../../common/services/enums/lifecycle-phase";
    import {criticality} from "../../common/services/enums/criticality";
    import _ from "lodash";
    import Markdown from "../../common/svelte/Markdown.svelte";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";

    export let primaryEntityRef;

    $: appCall = applicationStore.getById(primaryEntityRef.id);
    $: app = $appCall.data;

    let expanded = false;

    $: hasLongDescription = _.size(app.description) > 350;

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

    <div class={hasLongDescription && !expanded ? "waip-description" : ""}>
        <div class={hasLongDescription && !expanded ? "waip-description-fade" : ""}></div>
        <Markdown text={app.description}/>
    </div>
    {#if hasLongDescription}
        <button class="btn btn-skinny small pull-right"
                on:click={() => expanded = !expanded}>
            {expanded ? "Show less" : "Show more"}
        </button>
    {/if}

    <br>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>
{/if}


<style>

    .waip-description {
        position:relative;
        max-height:6em;
        overflow:hidden;
    }

    .waip-description-fade {
        position:absolute;
        top:2em;
        width:100%;
        height:4em;
        background: -webkit-linear-gradient(transparent, white);
        background: -o-linear-gradient(transparent, white);
        background: -moz-linear-gradient(transparent, white);
        background: linear-gradient(transparent, white);
    }


</style>