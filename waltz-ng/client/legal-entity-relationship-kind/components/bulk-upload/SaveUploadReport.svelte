<script>

    import {saveResponse} from "./bulk-upload-relationships-store";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";

    $: console.log({save: $saveResponse});

    $: relStats = $saveResponse.relationshipStats;
    $: assessmentStats = $saveResponse.assessmentStats;

</script>


<h4>Bulk Upload Report:</h4>

{#if relStats}
    <table class="table table-condensed">
        <colgroup>
            <col width="40%"/>
            <col width="60%"/>
        </colgroup>
        <thead>
        <tr>
            <th>Relationships</th>
            <th>Count</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>Created</td>
            <td>{relStats.addedCount}</td>
        </tr>
        <tr>
            <td>Updated</td>
            <td>{relStats.updatedCount}</td>
        </tr>
        <tr>
            <td>Removed</td>
            <td>{relStats.removedCount}</td>
        </tr>
        </tbody>
    </table>
    <div class="help-block"
         style="padding-bottom: 2em">
        <Icon name="info-circle"/>
        This table details all the changes that have been made to legal entity relationships, any rows with errors
        associated were ignored
    </div>
{/if}


{#if !_.isEmpty(relStats)}
    <table class="table table-condensed">
        <colgroup>
            <col width="40%"/>
            <col width="2%"/>
            <col width="20%"/>
            <col width="20%"/>
        </colgroup>
        <thead>
        <tr>
            <th>Assessment Definition</th>
            <th>Created</th>
            <th>Updated</th>
            <th>Removed</th>
        </tr>
        </thead>
        <tbody>
        {#each assessmentStats as stat}
            <tr>
                <td>{stat.definition.name}</td>
                <td>{stat.assessmentStatistics.addedCount}</td>
                <td>{stat.assessmentStatistics.updatedCount}</td>
                <td>{stat.assessmentStatistics.removedCount}</td>
            </tr>
        {/each}
        </tbody>
    </table>
    <div class="help-block"
         style="padding-bottom: 2em">
        <Icon name="info-circle"/>
        This table details all the changes that have been made to assessments against legal entity relationships, any
        rows with errors associated were ignored
    </div>
{/if}