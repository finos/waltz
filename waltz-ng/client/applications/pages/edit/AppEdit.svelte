<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {formData} from "./edit-store";
    import EntitySearchSelector from "../../../common/svelte/EntitySearchSelector.svelte";
    import _ from "lodash";
    import {applicationKind} from "../../../common/services/enums/application-kind";
    import {lifecyclePhase} from "../../../common/services/enums/lifecycle-phase";

    const kinds = _.values(applicationKind);
    const phases = _.values(lifecyclePhase);

    $: console.log($formData);

    function getRequiredFields(d) {
        return [d.name, d.orgUnit, d.phase, d.kind, d.rating];
    }

    $: invalid = _.some(
        getRequiredFields($formData),
        v => _.isEmpty(v));

    function save() {
        console.log("Saving", $formData)
    }
</script>


<PageHeader icon="edit"
            small={$formData?.name}
            name="App Edit">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li>Applications</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">

    <pre>{JSON.stringify($formData, "", null)}</pre>

    <form autocomplete="off"
          on:submit|preventDefault={save}>
        <div class="row">
            <div class="col-md-8">
                <div class="form-group">
                    <!-- NAME -->
                    <label for="name">
                        Name
                        <small class="text-muted">(required)</small>
                    </label>
                    <input class="form-control"
                           id="name"
                           required="required"
                           placeholder="Name of the application"
                           bind:value={$formData.name}/>
                    <div class="help-block">
                        Name of the application
                    </div>

                    <!-- ORG UNIT -->
                    <label for="name">
                        Owning Organisational Unit
                        <small class="text-muted">(required)</small>
                    </label>
                    <div>
                        <EntitySearchSelector on:select={e => $formData.orgUnit = e.detail.id}
                                              entityKinds={['ORG_UNIT']} />
                    </div>
                    <div class="help-block">
                        Organisational unit which owns this application
                    </div>

                    <!-- ORG UNIT -->
                    <label for="name">
                        Description
                        <small class="text-muted">(required)</small>
                    </label>
                    <textarea class="form-control"
                              bind:value={$formData.description}
                              rows="8"></textarea>
                    <div class="help-block">
                        Basic markdown formatting is supported
                    </div>
                </div>
            </div>
            <div class="col-md-4">

                <!-- ASSET CODE -->
                <label for="name">
                    Asset Code
                </label>
                <input class="form-control"
                       id="assetCode"
                       placeholder="Asset code"
                       bind:value={$formData.assetCode}/>
                <div class="help-block">
                    Asset code/external identifier for the application
                </div>

                <!-- OVERALL RATING -->
                <label for="name">
                    Overall Rating
                    <small class="text-muted">(required)</small>
                </label>
                <select class="form-control"
                        bind:value={$formData.rating}>
                    <option disabled value={undefined}> -- select a rating -- </option>
                    <option value="G">Invest</option>
                    <option value="R">Disinvest</option>
                    <option value="A">Hold</option>
                    <option value="Z">Unknown</option>
                </select>
                <div class="help-block">
                    Overall strategic rating for this application
                </div>

                <!-- TYPE -->
                <label for="name">
                    Type
                    <small class="text-muted">(required)</small>
                </label>
                <select class="form-control"
                        bind:value={$formData.kind}>
                    <option disabled value={undefined}> -- select a kind -- </option>
                    {#each kinds as kind}
                        <option value={kind.key}>
                            {kind.name}
                        </option>
                    {/each}
                </select>
                <div class="help-block">
                    Type of application, indicates it's origin
                </div>

                <!-- LIFECYCLE_PHASE -->
                <label for="name">
                    Lifecycle Phase
                    <small class="text-muted">(required)</small>
                </label>
                <select class="form-control"
                        bind:value={$formData.phase}>
                    <option disabled value={undefined}> -- select a phase -- </option>
                    {#each phases as phase}
                        <option value={phase.key}>
                            {phase.name}
                        </option>
                    {/each}
                </select>
                <div class="help-block">
                    Current lifecycle phase of the application
                </div>
                <!-- BUSINESS_CRITICALITY -->

            </div>
        </div>

        <button type="submit"
                class="btn btn-success"
                disabled={invalid}>
            Save
        </button>
    </form>

</div>