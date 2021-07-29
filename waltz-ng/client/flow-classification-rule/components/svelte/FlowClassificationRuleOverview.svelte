<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {flowClassificationRuleStore} from "../../../svelte-stores/flow-classification-rule-store";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";
    import {dataTypeStore} from "../../../svelte-stores/data-type-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";

    export let primaryEntityRef;

    let rulesCall = flowClassificationRuleStore.getById(primaryEntityRef.id)
    $: classificationRule = $rulesCall.data;

    let classificationsCall = flowClassificationStore.findAll();
    $: classifications = $classificationsCall.data;
    $: classificationsById = _.keyBy($classificationsCall.data, d => d.id);

    let datatypesCall = dataTypeStore.findAll();

    $: datatypes = $datatypesCall.data
    $: datatypesById = _.keyBy(datatypes, d => d.id);
    $: datatype = Object.assign({}, datatypesById[classificationRule.dataTypeId], {kind: "DATA_TYPE"});
    $: datatypeName = _.get(datatypesById, [classificationRule.dataTypeId, "name"], "unknown");
    $: rating = classificationsById[classificationRule.classificationId];
</script>

<PageHeader icon="shield"
            name={`Flow Classification Rule: ${classificationRule.applicationReference?.name}`}
            small={datatypeName}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">Flow Classification Rule</ViewLink></li>
            <li><EntityLink ref={classificationRule.applicationReference}/></li>
            <li><EntityLink ref={datatype}/></li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="waltz-display-section">

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Rating:
            </div>
            <div class="col-sm-4">
                <span class="indicator"
                      style={`background-color: ${rating?.color}`}>
                </span>
                <span title={rating?.description}>
                    {rating?.name}
                </span>
            </div>

            <div class="col-sm-2 waltz-display-field-label">
                Rating Description:
            </div>
            <div class="col-sm-4">
                {rating?.description || "-"}
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Source Application:
            </div>
            <div class="col-sm-4">
                <EntityLink ref={classificationRule.applicationReference}/>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Datatype:
            </div>
            <div class="col-sm-4">
                <EntityLink ref={datatype}/>
            </div>

            <div class="col-sm-2 waltz-display-field-label">
                Datatype Description:
            </div>
            <div class="col-sm-4">
                    {datatype.description || "-"}
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Scope:
            </div>
            <div class="col-sm-4">
                <EntityLink ref={classificationRule.parentReference}/>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Description:
            </div>
            <div class="col-sm-4">
                {classificationRule.description || "-"}
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Provenance:
            </div>
            <div class="col-sm-4">
                {classificationRule.provenance}
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                External Id:
            </div>
            <div class="col-sm-4">
                {classificationRule.externalId || "-"}
            </div>
        </div>

        <div class="row">
            <div class="col-sm-12">
                <div class="help-block pull-right">
                    Last updated: <LastEdited entity={classificationRule}/>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    .indicator {
        display: inline-block;
        height: 0.9em;
        width: 1em;
        border: 1px solid #ccc;
        border-radius: 2px;
    }
</style>