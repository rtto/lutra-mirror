
for (var treeview of document.getElementsByClassName("treeview")) {

    for (var list of treeview.querySelectorAll(".click")) {
        list.addEventListener("click", function() {
            this.nextSibling.classList.toggle("active");
            this.classList.toggle("caret-down");
        });
    }

    // simulate a click on the root node to expand it.
    treeview.firstChild.firstChild.click();
}
