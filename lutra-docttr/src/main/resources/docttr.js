var treeviewCounter = 0;
for (var treeview of document.getElementsByClassName("treeview")) {

    if (treeview.classList.contains('toggle-all')) {
        // add id to treeview so we can expandAll
        treeviewCounter += 1;
        treeview.setAttribute("id", "treeview" + treeviewCounter)
        treeview.insertAdjacentHTML('beforebegin',
          '<p class="treeviewbuttons"><span class="button" onclick="treeViewToggleAll(\'treeview' + treeviewCounter + '\', 1)">Expand all</span> '
          +  '<span class="button" onclick="treeViewToggleAll(\'treeview' + treeviewCounter + '\', 0)">Contract all</span></p>'
        );
    }

    if (treeview.classList.contains('click-first')) {
        // simulate a click on the root node to expand it.
        treeview.firstChild.firstChild.click();
    }

    if (treeview.classList.contains('expand-all')) {
        treeViewToggleAll('treeview' + treeviewCounter, 1);
    }
}

function treeViewToggleAll(elementid, open) {
    var treeview = document.getElementById(elementid);
    for (var list of treeview.querySelectorAll("details")) {
        if (open) {
            list.setAttribute("open", "open");
        } else {
            list.removeAttribute("open");
        }
    }
}