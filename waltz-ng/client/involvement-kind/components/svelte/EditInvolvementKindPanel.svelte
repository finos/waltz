<script>

    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let onCancel;
    export let onSave;
    export let involvementKind;

    const initialVal = Object.assign({}, involvementKind);

    let working = Object.assign({}, {
        externalId: involvementKind.externalId,
        description: involvementKind.description,
        name: involvementKind.name,
        userSelectable: involvementKind.userSelectable
    });

    function cancel() {
        working = initialVal;
        onCancel()
    }

    $: invalidKind = _.isNull(working.name)
        || _.isNull(working.description)
        || _.isNull(working.externalId)
        || _.isEmpty(working.name)
        || _.isEmpty(working.description)
        || _.isEmpty(working.externalId)


</script>

<div class="row">
    <div class="col-sm-2">
        Name
    </div>
    <div class="col-sm-10">
        <input class="form-control"
               id="name"
               placeholder="Name"
               bind:value={working.name}>
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        Description
    </div>
    <div class="col-sm-10">
        <input class="form-control"
               id="desc"
               placeholder="Description"
               bind:value={working.description}>
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        External Id
    </div>
    <div class="col-sm-10">
        <input class="form-control"
               id="extId"
               placeholder="External Id"
               bind:value={working.externalId}>
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        User Selectable
    </div>
    <div class="col-sm-10">
        <input id="userSelectable"
               type="checkbox"
               checked={working.userSelectable}
               on:click={() => working.userSelectable = !working.userSelectable}>
    </div>
</div>
<div class="row">
    <div class="col-sm-12">
        <button class="btn btn-skinny"
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