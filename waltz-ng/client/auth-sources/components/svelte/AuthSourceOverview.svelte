<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {authoritativeSourceStore} from "../../../svelte-stores/authoritative-source-store";
    import {dataTypeStore} from "../../../svelte-stores/data-type-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import {nestEnums} from "../../../common/svelte/enum-utils";
    import {enumValueStore} from "../../../svelte-stores/enum-value-store";

    export let primaryEntityRef;

    let authSourceCall = authoritativeSourceStore.getById(primaryEntityRef.id)
    let datatypesCall = dataTypeStore.findAll();
    let enumsCall = enumValueStore.load();

    $: authSource = $authSourceCall.data;

    $: datatypes = $datatypesCall.data
    $: datatypesByCode = _.keyBy(datatypes, d => d.code);
    $: datatype = Object.assign({}, datatypesByCode[authSource.dataType], {kind: "DATA_TYPE"});
    $: datatypeName = _.get(datatypesByCode, [authSource.dataType, "name"], "unknown");
    $: rating = _
        .chain($enumsCall.data)
        .filter(d => d.type === "AuthoritativenessRating")
        .find(d => d.key === authSource.rating)
        .value();
</script>

<PageHeader icon="shield"
            name={`Authoritative source: ${authSource.applicationReference?.name}`}
            small={datatypeName}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">Authoritative Source</ViewLink></li>
            <li><EntityLink ref={authSource.applicationReference}/></li>
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
                      style={`background-color: ${rating?.iconColor}`}>
                </span>
                <span title={rating?.description}>
                    {rating?.name}
                </span>
            </div>

            <div class="col-sm-6">
                <p class="help-block">
                    {rating?.description}
                </p>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Source Application:
            </div>
            <div class="col-sm-4">
                <EntityLink ref={authSource.applicationReference}/>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Datatype:
            </div>
            <div class="col-sm-4">
                <EntityLink ref={datatype}/>
            </div>

            <div class="col-sm-6">
                <p class="help-block">
                    {datatype.description}
                </p>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Scope:
            </div>
            <div class="col-sm-4">
                <EntityLink ref={authSource.parentReference}/>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Description:
            </div>
            <div class="col-sm-4">
                {authSource.description || "-"}
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                Provenance:
            </div>
            <div class="col-sm-4">
                {authSource.provenance}
            </div>
        </div>

        <div class="row">
            <div class="col-sm-2 waltz-display-field-label">
                External Id:
            </div>
            <div class="col-sm-4">
                {authSource.externalId || "-"}
            </div>
        </div>

        <div class="row">
            <div class="col-sm-12">
                <div class="help-block pull-right">
                    Last updated: <LastEdited entity={authSource}/>
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