<script>

    import {createEventDispatcher, onMount} from "svelte";

    export let diagram;

    let working = {
        id: null,
        name:  null,
        group:  null,
        description: null,
        svg:  null,
        priority:  null,
        keyProperty: null,
        product: null,
        displayHeightPercent: null,
        displayWidthPercent: null
    }

    const dispatch = createEventDispatcher();

    onMount(() => {
        working = Object.assign({}, working, diagram)
    })

    function cancel() {
        dispatch("cancel");
    }

    function onSave() {
        dispatch("save", working);
    }

</script>

<div>
    <div class="row">
        <div class="col-md-12">
            <form autocomplete="off"
                  on:submit|preventDefault={onSave}>

                <div class="form-group">
                    <label for="name">Name<span style="color: red" title="This field is mandatory">*</span></label>
                    <input type="text"
                           class="form-control"
                           id="name"
                           bind:value={working.name}
                           placeholder="Name"
                           required>
                </div>
                <div class="form-group">
                    <label for="group">Group<span style="color: red" title="This field is mandatory">*</span></label>
                    <input type="text"
                           class="form-control"
                           id="group"
                           bind:value={working.group}
                           placeholder="Group"
                           required>
                    <div class="help-block">
                        This is used to indicate where to display the diagram. If diagrams share a group they will appear in tabs.
                    </div>
                </div>
                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea class="form-control"
                              id="description"
                              bind:value={working.description}
                              placeholder="Description"
                              rows="3"/>
                    <div class="help-block">
                        Description of the diagram. Markdown is supported.
                    </div>
                </div>
                <div class="form-group">
                    <label for="priority">Priority<span style="color: red" title="This field is mandatory">*</span></label>
                    <input class="form-control"
                           type="number"
                           id="priority"
                           style="width: 20%"
                           required="required"
                           placeholder="Priority for this diagram in when ordered within a group"
                           bind:value={working.priority}>
                    <div class="help-block">
                        Priority, used for ordering diagrams.
                        Lower numbers go first, name is used as a tie breaker.
                    </div>
                </div>
                <div class="form-group">
                    <label for="keyProperty">Key Property<span style="color: red" title="This field is mandatory">*</span></label>
                    <input type="text"
                           class="form-control"
                           id="keyProperty"
                           bind:value={working.keyProperty}
                           placeholder="Key Property"
                           required>
                    <div class="help-block">
                        This is used to indicate the attribute which holds data for this diagram (to render links).
                    </div>
                </div>
                <div class="form-group">
                    <label for="group">Product<span style="color: red" title="This field is mandatory">*</span></label>
                    <input type="text"
                           class="form-control"
                           id="product"
                           bind:value={working.product}
                           placeholder="none"
                           required>
                    <div class="help-block">
                        The product used to produce the diagram.
                    </div>
                </div>

                <div class="form-group">
                    <label for="displayWidthPercent">Display Width Percent</label>
                    <input class="form-control"
                           type="number"
                           id="displayWidthPercent"
                           style="width: 20%"
                           bind:value={working.displayWidthPercent}>
                    <div class="help-block">
                        Used to size the diagram
                    </div>
                </div>

                <div class="form-group">
                    <label for="displayHeightPercent">Display Height Percent</label>
                    <input class="form-control"
                           type="number"
                           id="displayHeightPercent"
                           style="width: 20%"
                           bind:value={working.displayHeightPercent}>
                    <div class="help-block">
                        Used to size the diagram
                    </div>
                </div>


                <div class="form-group">
                    <label for="diagram">Diagram<span style="color: red" title="This field is mandatory">*</span></label>
                    <textarea class="form-control"
                              id="diagram"
                              bind:value={working.svg}
                              rows="6"
                              required/>
                </div>


                <button type="submit"
                        class="btn btn-primary">
                    Save
                </button>
                <button class="btn btn-default"
                        on:click|preventDefault={cancel}>
                    Cancel
                </button>
            </form>
        </div>
    </div>
</div>