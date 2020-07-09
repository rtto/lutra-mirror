var treeviewCounter = 0;
for (var treeview of document.getElementsByClassName("treeview")) {

    for (var list of treeview.querySelectorAll(".click")) {
        list.addEventListener("click", function() {
            this.nextSibling.classList.toggle("active");
            this.classList.toggle("caret-down");
        });
    }

    // simulate a click on the root node to expand it.
    treeview.firstChild.firstChild.click();

    // add id to treeview so we can expandAll
    treeviewCounter += 1;
    treeview.setAttribute("id", "treeview" + treeviewCounter)
    treeview.insertAdjacentHTML('beforebegin',
      '<p class="treeviewbuttons"><span class="button" onclick="treeViewToggleAll(\'treeview' + treeviewCounter + '\', 1)">Expand all</span> '
      +  '<span class="button" onclick="treeViewToggleAll(\'treeview' + treeviewCounter + '\', 0)">Contract all</span></p>'
    );
}

function treeViewToggleAll(elementid, open) {
    var treeview = document.getElementById(elementid);
    for (var list of treeview.querySelectorAll(".click")) {
        if (open) {
            list.nextSibling.classList.add("active");
            list.classList.add("caret-down");
        } else {
            list.nextSibling.classList.remove("active");
            list.classList.remove("caret-down");
        }
    }
}