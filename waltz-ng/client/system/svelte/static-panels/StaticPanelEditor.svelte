<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let doCancel;
    export let doSave;
    export let panel;

    let workingCopy = Object.assign({}, panel);
    let invalid = true;

    $: {
        const badTextFields = _.some(
            [workingCopy.group, workingCopy.content],
            v => _.isEmpty(v));

        invalid = badTextFields
            || workingCopy.priority < 0
            || workingCopy.width < 1
            || workingCopy.width > 12;
    }


</script>


<form autocomplete="off">

    <div class="row">
        <div class="col-md-8">
            <div class="form-group">
                <label for="group">
                    Group
                    <small class="text-muted">(required)</small>
                </label>
                <input class="form-control"
                       id="group"
                       required="required"
                       placeholder="Group, e.g. HOME"
                       bind:value={workingCopy.group}>
                <div class="help-block">
                    Group determines on what screens this panel will appear
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="form-group">
                <label for="priority">
                    Priority
                    <small class="text-muted">(required)</small>
                </label>
                <input class="form-control"
                       id="priority"
                       type="number"
                       min="0"
                       required="required"
                       placeholder="Priority"
                       bind:value={workingCopy.priority}>
                <div class="help-block">
                    If multiple panels belong to the same group, priority will be used to order them (ascending)
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-8">
            <div class="form-group">
                <label for="title">
                    Title
                </label>
                <input class="form-control"
                       id="title"
                       placeholder="Title"
                       bind:value={workingCopy.title}>
                <div class="help-block">
                    Only shown if panel rendered in a section
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="form-group">
                <label for="icon">
                    Icon
                </label>
                <input class="form-control"
                       id="icon"
                       placeholder="Link Title"
                       bind:value={workingCopy.icon}>
                <div class="help-block">
                    Icon names are base on fontawesome names (without the 'fa-' prefix)
                </div>
                <div>
                    Preview:
                    <Icon name={workingCopy.icon}/>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-8">
            <label for="priority">
                Content
                <small class="text-muted">(required)</small>
            </label>
            <textarea id="content"
                      class="form-control"
                      rows="12"
                      required="required"
                      bind:value={workingCopy.content}/>
            <div class="help-block">
                HTML or markdown code, any paths should be absolute
            </div>
        </div>

        <div class="col-md-4">
            <div class="form-group">
                <label for="title">
                    Width
                    <small class="text-muted">(required)</small>
                </label>
                <input class="form-control"
                       id="width"
                       type="number"
                       min="1"
                       max="12"
                       required="required"
                       placeholder="Priority"
                       bind:value={workingCopy.width}>
                <div class="help-block">
                    If rendered in a grid determines the number of cells to span (1-12)
                </div>
            </div>
        </div>
    </div>

    <button type="submit"
            class="btn btn-success"
            disabled={invalid}
            on:click|preventDefault={() => doSave(workingCopy)}>
        Save
    </button>

    <button class="btn btn-link"
            on:click={doCancel}>
        Cancel
    </button>
</form>

<style>
    input:invalid {
        border: 2px solid red;
    }

    textarea:invalid {
        border: 2px solid red;
    }
</style>