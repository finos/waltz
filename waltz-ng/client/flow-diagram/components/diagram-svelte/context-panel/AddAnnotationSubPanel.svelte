<script>
    import {processor} from "../diagram-model-store";
    import {toGraphId} from "../../../flow-diagram-utils";
    import {createEventDispatcher} from "svelte";

    export let selected;
    const dispatch = createEventDispatcher();

    let note = selected.note || "";

    function mkNewAnnotation(selected) {
        return Object.assign(
            {},
            {
                note,
                id: +new Date() + '',
                kind: 'ANNOTATION',
                entityReference: selected
            });
    }

    function saveAnnotation() {
        const commands = [];


        if (selected.kind === 'ANNOTATION') {
            commands.push({
                command: 'UPDATE_ANNOTATION',
                payload: {
                    note: note,
                    id: toGraphId(selected)
                }
            });
        } else {
            const payload = mkNewAnnotation(selected);

            commands.push({
                command: 'ADD_ANNOTATION',
                payload
            });

            commands.push({
                command: 'MOVE',
                payload: {
                    id: toGraphId(payload),
                    dx: 30,
                    dy: 30
                }
            });
        }
        $processor(commands);
        cancel();
    }

    function cancel(){
        dispatch("cancel");
    }

</script>

<div>
    <p>Annotation text:</p>
    <textarea id="description"
              class="form-control"
              rows="4"
              style="width: 100%"
              required="required"
              bind:value={note}/>
</div>
<span>
    <button class="btn btn-skinny" on:click={cancel}>
        Cancel
    </button>
    |
    <button class="btn btn-skinny" on:click={saveAnnotation}>
        Update text
    </button>
</span>
<style>
</style>