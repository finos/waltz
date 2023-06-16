<script>


    import {involvementKindStore} from "../../../svelte-stores/involvement-kind-store";
    import toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EditInvolvementKindPanel from "./EditInvolvementKindPanel.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import _ from "lodash";
    import EntityIcon from "../../../common/svelte/EntityIcon.svelte";

    export let involvementKind;
    export let reload = () => "reloading Kind";

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    let activeMode = Modes.VIEW;

    function cancel() {
        activeMode = Modes.VIEW;
    }

    function save(working, initialVal) {

        const name = {
            newVal: working.name,
            oldVal: initialVal.name
        };

        const description = {
            newVal: working.description,
            oldVal: initialVal.description
        };

        const externalId = {
            newVal: working.externalId,
            oldVal: initialVal.externalId
        };

        const userSelectable = {
            newVal: working.userSelectable,
            oldVal: initialVal.userSelectable
        };

        const permittedRole = {
            newVal: working.permittedRole,
            oldVal: initialVal.permittedRole
        };

        const change = Object.assign({}, {
            id: initialVal.id,
            name,
            description,
            externalId,
            userSelectable,
            permittedRole
        });

        return involvementKindStore
            .update(change)
            .then(() => {
                toasts.success("Updated");
                reload(initialVal.id);
                cancel();
            })
            .catch(e => {
                displayError(`Failed to apply change: ${JSON.stringify(change)}`, e)
                cancel();
            });
    }

</script>


{#if activeMode === Modes.VIEW}
    <div class="row">
        <div class="col-sm-2">
            Name
        </div>
        <div class="col-sm-10">
            {involvementKind?.name}
        </div>
    </div>
    <div class="row">
        <div class="col-sm-2">
            Subject Kind
        </div>
        <div class="col-sm-10">
            <EntityIcon kind={involvementKind?.subjectKind}/>
            {_.get(entity, [involvementKind?.subjectKind, "name"], "-")}
        </div>
    </div>
    <div class="row">
        <div class="col-sm-2">
            Description
        </div>
        <div class="col-sm-10">
            {involvementKind?.description || "-"}
        </div>
    </div>
    <div class="row">
        <div class="col-sm-2">
            External Id
        </div>
        <div class="col-sm-10">
            {involvementKind?.externalId || "-"}
        </div>
    </div>
    <div class="row">
        <div class="col-sm-2">
            User Selectable
        </div>
        <div class="col-sm-10">
            {#if involvementKind?.userSelectable}
                <span style="color:lightgreen"><Icon name="check"/></span>
            {:else}
                <span style="color:lightcoral"><Icon name="times"/></span>
            {/if}
        </div>
    </div>
    <div class="row">
        <div class="col-sm-2">
            Permitted Role
        </div>
        <div class="col-sm-10">
            {involvementKind?.permittedRole || "-"}
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12" style="padding-top: 1em">
            <button class="btn btn-skinny"
                    on:click={() => activeMode = Modes.EDIT}>
                <Icon name="pencil"/>
                Edit
            </button>
        </div>
    </div>

{:else if activeMode === Modes.EDIT}
    <EditInvolvementKindPanel onCancel={cancel}
                              onSave={save}
                              {involvementKind}/>
{/if}