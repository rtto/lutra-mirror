
document.body.insertAdjacentHTML('afterbegin', '<span id="ToCtop"></span>');
document.body.insertAdjacentHTML('afterbegin', '<div id="ToC"></div>');
toc = document.getElementById("ToC");//Add a header
tocHeader = document.createElement("b");
tocHeader.innerText="Contents";
toc.appendChild(tocHeader); // Get the h3 tags — ToC entries
headers = document.getElementsByTagName("h2");
tocList = document.createElement("ul");

 for (i = 0; i < headers.length; i++){

   // Create an id
   name = "h"+i;
   headers[i].id=name;

   // a list item for the entry
   tocListItem = document.createElement("li");

   // a link for the h3
   tocEntry = document.createElement("a");
   tocEntry.setAttribute("href","#"+name);
   tocEntry.innerText=headers[i].innerText;
   tocListItem.appendChild(tocEntry);
   tocList.appendChild(tocListItem);
 }
tocList.insertAdjacentHTML('beforeend', '<li><a href="#ToCtop">top</a></li>');
 toc.appendChild(tocList);

