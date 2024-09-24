<script>

    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import _ from "lodash";
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";
    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import { dataTypeStore } from "../../../svelte-stores/data-type-store";
    import {displayError} from "../../../common/error-utils";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    const Modes = {
        MIGRATE_PICKER : "MIGRATE_PICKER",
        MIGRATE_OPTIONS : "MIGRATE_OPTIONS",
        MIGRATE_CONFIRM : "MIGRATE_CONFIRM",
        MIGRATE_RESULT : "MIGRATE_RESULT",
        WAITING : "WAITING",
        MIGRATE_ERROR : "MIGRATE_ERROR"
    };

    let mode = Modes.MIGRATE_PICKER;

    let sourceDataType = null;
    let targetDataType = null;
    let canApply = false;
    let removeSource = false;
    let migrateResult = null;

    function onSourceDataTypeSelect(evt) {
        sourceDataType = evt.detail;
    }

    function onTargetDataTypeSelect(evt) {
        const dataType = evt.detail;
        if (!dataType.concrete){
            toasts.warning("You must select a concrete data type as the merge target");
            return;
        }
        if (dataType.deprecated){
            toasts.warning("You cannot select a deprecated data type as the merge target");
            return;
        }
        if (dataType.id === sourceDataType.id){
            toasts.warning("You cannot migrate from and to the same data type");
            return;
        }
        targetDataType = dataType;
        mode = Modes.MIGRATE_OPTIONS;
    }

    function onTargetDataTypeChange() {
        targetDataType = null;
        mode = Modes.MIGRATE_PICKER;
    }

    function doMigrate() {
        mode = Modes.WAITING;
        dataTypeStore
            .migrate(sourceDataType.id, targetDataType.id, removeSource)
            .then(r => {
                migrateResult = r.data;
                mode = Modes.MIGRATE_RESULT;
            })
            .catch(e => {
                displayError("Could not migrate", e);
                mode = Modes.MIGRATE_ERROR;
            });
    }

    function onMigrate() {
        mode = Modes.MIGRATE_CONFIRM;
    }

    function onConfirmationCancel() {
        mode = Modes.MIGRATE_OPTIONS;
    }

    function onDoneMigrate() {
        mode = Modes.MIGRATE_PICKER;
        sourceDataType = null;
        targetDataType = null;
        removeSource = false;
        migrateResult = null;
    }

    $: canApply = _.isEmpty(errors);

    $: {
        if(sourceDataType) {
            if (!_.isEmpty(sourceDataType.children) && removeSource) {
                toasts.warning('Cannot delete a data type that has children');
                canApply = false;                        
            } else {
                canApply = true;
            }
        }
    };

    $: errors = _.compact([
        sourceDataType ? null : 'NO_DATA_TYPE_SOURCE',
        targetDataType ? null : 'NO_DATA_TYPE_TARGET',
    ]);

</script>

<PageHeader icon="wrench"
            name="Data Types"
            small="maintain">

    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.data-type.list">Data Types</ViewLink>
            </li>
            <li>
                Maintain
            </li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="waltz-display-section">
        <div class="help-block">
            Select a data type you wish to modify.
        </div>
        <div class="row">
            <div class="col-md-6">
                <b>From</b>
                <DataTypeTreeSelector multiSelect={false}
                    on:select={onSourceDataTypeSelect}/>
            </div>
            <div class="col-md-6">
                {#if sourceDataType}
                <SubSection>
                    <div slot="header">
                        Actions
                    </div>
                    <div slot="content">
                            Selected Data Type: <EntityLabel ref={sourceDataType}/>
                            <details open style="margin-top:1em">
                                <summary>Migrate <Icon name="code-fork"/></summary>
                                <div style="padding-left:1em">
                                    <div class="help-block">
                                        This action migrates data type usage from the source data type to a destination data type. 
                                        You may optionally remove the source data type when the migration is completed.
                                    </div>
                                    {#if mode === Modes.MIGRATE_PICKER}
                                        <div class="help-block"> Select a data type to merge into, this must be concrete and non-deprecated</div>
                                        <DataTypeTreeSelector multiSelect={false}
                                        on:select={onTargetDataTypeSelect}/>
                                    {/if}
                                    {#if mode === Modes.MIGRATE_OPTIONS}
                                        <div class="help-block"> If you choose to remove the source data type, be aware this cannot be undone</div>
                                        <form autocomplete="off" on:submit|preventDefault={doMigrate}>
                                            <b>To</b>
                                                <EntityLabel ref={targetDataType}/>
                                                <br>
                                                <button style="padding-left:2.6em" 
                                                        class="btn btn-skinny" 
                                                        on:click={onTargetDataTypeChange}>
                                                    Change
                                                </button>
                                            <br>
                                            <label for="removeSource">Remove source?</label>
                                            <input type="checkbox" 
                                                    id="removeSource" 
                                                    bind:checked={removeSource}/>
                                            <hr>
                                            <button type="submit" 
                                                    class="btn btn-xs btn-primary" 
                                                    on:click={onMigrate} 
                                                    disabled={!canApply}>
                                                Migrate &raquo;
                                            </button>
                                        </form>
                                    {/if}
                                    {#if mode === Modes.MIGRATE_CONFIRM}
                                        <div class="alert alert-warning">
                                            Are you sure you wish to migrate from <EntityLabel ref={sourceDataType}/> to <EntityLabel ref={targetDataType}/>?
                                            {#if removeSource}
                                                <br>
                                                Once migration is complete the source data type will be removed
                                            {/if}
                                            <hr>
                                            <button class="btn btn-sm btn-danger" on:click={doMigrate}>Proceed</button>
                                            <button class="btn btn-skinny" on:click={onConfirmationCancel}>Cancel</button>
                                        </div>
                                    {/if}
                                    {#if mode === Modes.WAITING}
                                        <h3>Please wait...</h3>
                                    {/if}
                                    {#if mode === Modes.MIGRATE_RESULT}
                                        <table class="waltz-field-table waltz-field-table-border" style="width:100%; margin-bottom:2em;">
                                            <tr>
                                                <td>Logical Flow Data Types Changed</td>
                                                <td>{migrateResult.logicalFlowDataTypeCount}</td>
                                            </tr>
                                            <tr>
                                                <td>Physical Specification Data Types Changed</td>
                                                <td>{migrateResult.physicalSpecDataTypeCount}</td>

                                            </tr>
                                            <tr>
                                                <td>Classification Rules Changed</td>
                                                <td>{migrateResult.classificationRuleCount}</td>
                                            </tr>
                                            <tr>
                                                <td>Data Type Usages Changed</td>
                                                <td>{migrateResult.usageCount}</td>
                                            </tr>
                                            <tr>
                                                <td>Source Data Type Removed?</td>
                                                <td>{migrateResult.dataTypeRemoved}</td>
                                            </tr>
                                        </table>
                                        <button class="btn btn-skinny" on:click={onDoneMigrate}>Done</button>
                                    {/if}
                                </div>
                            </details> 
                    </div>
                </SubSection>
                {:else}
                <SubSection>
                    <div slot="header">
                        Pending changes
                    </div>
                    <div slot="content">
                        Waiting for Selection...  
                    </div> 
                </SubSection> 
                {/if}
            </div>
        </div>
    </div>    
</div>
