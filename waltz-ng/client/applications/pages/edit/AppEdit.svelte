<script>
    import _ from "lodash";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import EntitySearchSelector from "../../../common/svelte/EntitySearchSelector.svelte";
    import {formData} from "./edit-store";
    import {applicationKind} from "../../../common/services/enums/application-kind";
    import {lifecyclePhase} from "../../../common/services/enums/lifecycle-phase";
    import {criticality} from "../../../common/services/enums/criticality";

    const kinds = _.values(applicationKind);
    const phases = _.values(lifecyclePhase);
    const criticalities = _.values(criticality);

    function getRequiredFields(d) {
        return [
            d.name,
            d.organisationalUnitId,
            d.lifecyclePhase,
            d.applicationKind,
            d.businessCriticality,
            d.overallRating
        ];
    }

    $: invalid = console.log(getRequiredFields($formData)) || _.some(
        getRequiredFields($formData),
        v => _.isNumber(v)
            ? _.isNil(v)
            : _.isEmpty(v));

    function save() {
        console.log("Saving", $formData);
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
                           maxlength="255"
                           required="required"
                           placeholder="Name of the application"
                           bind:value={$formData.name}/>
                    <div class="help-block">
                        Name of the application
                    </div>

                    <!-- ORG UNIT -->
                    <label for="org-unit">
                        Owning Organisational Unit
                        <small class="text-muted">(required)</small>
                    </label>
                    <div id="org-unit">
                        <EntitySearchSelector on:select={e => $formData.organisationalUnitId = e.detail?.id}
                                              entityKinds={['ORG_UNIT']} />
                    </div>
                    <div class="help-block">
                        Organisational unit which owns this application
                    </div>

                    <!-- ORG UNIT -->
                    <label for="description">
                        Description
                        <small class="text-muted">(required)</small>
                    </label>
                    <textarea class="form-control"
                              id="description"
                              bind:value={$formData.description}
                              maxlength="4000"
                              rows="11"></textarea>
                    <div class="help-block">
                        Basic markdown formatting is supported
                    </div>
                </div>
            </div>
            <div class="col-md-4">

                <!-- ASSET CODE -->
                <label for="assetCode">
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
                <label for="rating">
                    Overall Rating
                    <small class="text-muted">(required)</small>
                </label>
                <select class="form-control"
                        id="rating"
                        bind:value={$formData.overallRating}>
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
                <label for="type">
                    Type
                    <small class="text-muted">(required)</small>
                </label>
                <select class="form-control"
                        id="type"
                        bind:value={$formData.applicationKind}>
                    <option disabled value={undefined}> -- select a kind -- </option>
                    {#each kinds as option}
                        <option value={option.key}>
                            {option.name}
                        </option>
                    {/each}
                </select>
                <div class="help-block">
                    Type of application, indicates it's origin
                </div>

                <!-- LIFECYCLE_PHASE -->
                <label for="phase">
                    Lifecycle Phase
                    <small class="text-muted">(required)</small>
                </label>
                <select class="form-control"
                        id="phase"
                        bind:value={$formData.lifecyclePhase}>
                    <option disabled value={undefined}> -- select a phase -- </option>
                    {#each phases as option}
                        <option value={option.key}>
                            {option.name}
                        </option>
                    {/each}
                </select>
                <div class="help-block">
                    Current lifecycle phase of the application
                </div>

                <!-- BUSINESS_CRITICALITY -->
                <label for="criticality">
                    Business Criticality
                    <small class="text-muted">(required)</small>
                </label>
                <select class="form-control"
                        id="criticality"
                        bind:value={$formData.businessCriticality}>
                    <option disabled value={undefined}> -- select a criticality -- </option>
                    {#each criticalities as option}
                        <option value={option.key}>
                            {option.name}
                        </option>
                    {/each}
                </select>
                <div class="help-block">
                    Criticality rating of application to business
                </div>

            </div>
        </div>

        <button type="submit"
                class="btn btn-success"
                disabled={invalid}>
            Save
        </button>
    </form>

</div>