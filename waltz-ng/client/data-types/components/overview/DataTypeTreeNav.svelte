<script>
    import DataTypeTreeNode from "../../../common/svelte/DataTypeTreeNode.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import pageInfo from "../../../svelte-stores/page-navigation-store";
    export let node = null;

    function go(evt) {
        pageInfo.set({
            state: "main.data-type.view",
            params: {id: evt.detail.id}
        });
    }
</script>

<SubSection>
    <div slot="header">Hierarchy</div>
    <div slot="content">
        <DataTypeTreeNode nonConcreteSelectable={true}
                          isRoot={false}
                          {node}
                          childNodes={node.children}
                          expanded={true}
                          on:select={go}/>
    </div>
</SubSection>
