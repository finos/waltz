<script>

    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import EntityIcon from "../../../common/svelte/EntityIcon.svelte";

    export let onCancel;
    export let onSave;
    export let involvementKind;

    const initialVal = Object.assign({}, involvementKind);

    let working = Object.assign({}, {
        externalId: involvementKind.externalId,
        description: involvementKind.description,
        name: involvementKind.name,
        userSelectable: involvementKind.userSelectable,
        permittedRole: involvementKind.permittedRole,
        transitive: involvementKind.transitive
    });

    function cancel() {
        working = initialVal;
        onCancel()
    }

    $: invalidKind = _.isEmpty(working.name)
        || _.isEmpty(working.description)
        || _.isEmpty(working.externalId)


</script>

<form autocomplete="off"
      on:submit|preventDefault={() => onSave(working, initialVal)}>
    <div class="row">
        <div class="col-sm-3">
            Name *
        </div>
        <div class="col-sm-9">
            <input class="form-control"
                   id="name"
                   required
                   placeholder="Name"
                   bind:value={working.name}>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-3 waltz-display-field-label">
            Subject Kind
        </div>
        <div class="col-sm-9">
            <EntityIcon kind={involvementKind?.subjectKind}/>
            {_.get(entity, [involvementKind?.subjectKind, "name"], "-")}
            <div class="help-block">
                Indicates what type of entity this involvement can be attached to.
                Note, this cannot be edited once the involvement kind has been created.
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-3">
            Description *
        </div>
        <div class="col-sm-9">
            <input class="form-control"
                   id="desc"
                   required
                   placeholder="Description"
                   bind:value={working.description}>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-3">
            External Id *
        </div>
        <div class="col-sm-9">
            <input class="form-control"
                   id="extId"
                   required
                   placeholder="External Id"
                   bind:value={working.externalId}>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-3">
            Transitive
        </div>
        <div class="col-sm-9">
            <input id="transitive"
                   type="checkbox"
                   checked={working.transitive}
                   on:click={() => working.transitive = !working.transitive}>
            <div class="help-block">
                Transitive involvements are used when involvements assigned to people in a managers reporting tree should also be reported against the manager.
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-3">
            User Selectable
        </div>
        <div class="col-sm-9">
            <input id="userSelectable"
                   type="checkbox"
                   checked={working.userSelectable}
                   on:click={() => working.userSelectable = !working.userSelectable}>
            <div class="help-block">
                If checked then this involvement can be set by users (subject to the permitted role restriction)
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-3">
            Permitted Role
        </div>
        <div class="col-sm-9">
            <input class="form-control"
                   id="permittedRole"
                   placeholder="Permitted Role"
                   bind:value={working.permittedRole}>
            <div class="help-block">
                If set, then the editing user must have this role to assign this involvement
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12">
            <button class="btn btn-skinny"
                   type="submit"
                    disabled={invalidKind}
                    on:click={() => onSave(working, initialVal)}>
                <Icon name="floppy-o"/>
                Save
            </button>
            <button class="btn btn-skinny"
                    on:click={() => cancel()}>
                <Icon name="ban"/>
                Cancel
            </button>
        </div>
    </div>
</form>