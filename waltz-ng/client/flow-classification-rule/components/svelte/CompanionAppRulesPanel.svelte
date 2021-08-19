<script>
    import {flowClassificationRuleStore} from "../../../svelte-stores/flow-classification-rule-store"
    import {dataTypeStore} from "../../../svelte-stores/data-type-store";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";

    export let primaryEntityRef;

    $: companionAppRulesCall = flowClassificationRuleStore.findCompanionAppRulesById(primaryEntityRef?.id);
    $: companionRules = $companionAppRulesCall.data

    $: dataTypesCall = dataTypeStore.findAll();
    $: dataTypesById = _.keyBy($dataTypesCall.data, d => d?.id);

    $: classificationsCall = flowClassificationStore.findAll();
    $: classificationsById = _.keyBy($classificationsCall.data, d => d?.id);


</script>

<div class="help-block">
    <Icon name="info-circle"/>
    The following rules share the same source application as the parent flow classification rule
</div>

{#if !_.isEmpty(companionRules)}
    <div class:waltz-scroll-region-350={_.size(companionRules) > 10}>
        <table class="table table-hover">
            <thead>
                <tr>
                    <th width="20%"><Icon name="fw"/> Source App</th>
                    <th width="20%">Data Type</th>
                    <th width="20%">Scope</th>
                    <th width="15%">Classification</th>
                    <th width="10%">Provenance</th>
                    <th width="15%">Comments</th>
                </tr>
            </thead>
            <tbody>
            {#each companionRules as rule}
                <tr>
                    <td>
                        <EntityLink ref={{kind: 'FLOW_CLASSIFICATION_RULE', id: rule.id, name: rule.applicationReference.name}}/>
                    </td>
                    <td>
                        <EntityLink showIcon={false}
                                    ref={{kind: 'FLOW_CLASSIFICATION_RULE', id: rule.id, name: dataTypesById[rule.dataTypeId].name}}/>
                    </td>
                    <td>
                        <EntityLink showIcon={false}
                                    ref={{kind: 'FLOW_CLASSIFICATION_RULE', id: rule.id, name:rule.parentReference.name}}/>
                    </td>
                    <td>
                        <div class="rating-indicator-block"
                             style="background-color: {_.get(classificationsById, [rule.classificationId, 'color'], '#ccc')}">
                        </div>
                        {_.get(classificationsById, [rule.classificationId, 'name'], 'unknown')}</td>
                    <td>{rule.provenance}</td>
                    <td>{rule.description || "-"}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
{:else}
    <NoData>
        There are no flow classification rules which share this source application
    </NoData>
{/if}


<style>
    .rating-indicator-block {
        display: inline-block;
        width: 1em;
        height: 1.1em;
        border: 1px solid #aaa;
        border-radius: 2px;
        position: relative;
        top: 2px;
    }
</style>