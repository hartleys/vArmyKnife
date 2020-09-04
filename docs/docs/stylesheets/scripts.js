
console.log("LOADING SCRIPT.JS");





listH6 = document.querySelectorAll("h6")

for( var i = 0; i < listH6.length; i = i + 2){
	var cc = listH6[i];
	cc.classList.add("exampleCode");
	var dd = document.createElement("div");
	dd.classList.add("exampleCollapseSection");
	var nxt = cc.nextElementSibling;
	nxt.parentElement.insertBefore(dd,nxt);
	while( nxt !== null && nxt.tagName != "H6" ){
		var nn = nxt;
		nxt = nn.nextElementSibling;
		dd.appendChild(nn);
	}
	if( nxt !== null && nxt.tagName == "H6" ){
		nxt.parentElement.removeChild(nxt);
	}
	if( nxt === null ){
		console.log("warning: no end example?");
		console.log(cc);
	}
}

listH1 = document.querySelectorAll("h1")

for( var i = 0; i < listH1.length; i++){
	var cc = listH1[i];
	cc.classList.add("collapsible");
	var dd = document.createElement("div");
	dd.classList.add("collapseSection");
	var nxt = cc.nextElementSibling;
	nxt.parentElement.insertBefore(dd,nxt);
	while( nxt !== null && nxt.tagName != "H1" ){
		var nn = nxt;
		nxt = nn.nextElementSibling;
		dd.appendChild(nn);
	}
}



var coll = document.getElementsByClassName("collapsible");
var i;

for (var i = 0; i < coll.length; i++) {
  var cc = coll[i];
  cc.ELEMS_CONTENT = cc.nextElementSibling;

    /*cc.MODALITY = "SIMPLE"
      cc.addEventListener("click", function() {

        this.classList.toggle("active");
        if (this.ELEMS_CONTENT.style.display === "none") {
          this.ELEMS_CONTENT.style.display = "block";
        } else {
          this.ELEMS_CONTENT.style.display = "none";
        }
      });*/
}
var collEx = document.getElementsByClassName("exampleCode");

for (var i = 0; i < collEx.length; i++) {
  var cc = collEx[i];
  cc.ELEMS_CONTENT = cc.nextElementSibling;
  /*cc.ELEMS_CONTENT.style.display = "none"

    cc.MODALITY = "SIMPLE"
      cc.addEventListener("click", function() {

        this.classList.toggle("active");
        if (this.ELEMS_CONTENT.style.display === "none") {
          this.ELEMS_CONTENT.style.display = "block";
        } else {
          this.ELEMS_CONTENT.style.display = "none";
        }
      });*/
	cc.isGrouped = false;
}

i=1
for (var i = 0; i < collEx.length; i++) {
  var cc = collEx[i];
  if(! cc.isGrouped){
	console.log("starting group")
    cc.isGrouped = true;
	var groupHolder = document.createElement("div");
	groupHolder.classList.add("exampleBox");
	cc.parentElement.insertBefore(groupHolder,cc);
	var curr = cc.nextElementSibling;
	//groupHolder.textContent = "EXAMPLES:"
	groupHolder.appendChild(cc);
	
	while( curr !== null && ( curr.classList.contains("exampleCode") || curr.classList.contains("exampleCollapseSection") )){
		console.log("adding to group: "+curr.classList)
		var prev = curr;
		prev.isGrouped = true;
		curr = curr.nextElementSibling;
		groupHolder.appendChild(prev);
	}
	console.log("curr = ");
	console.log(curr)
  }
  
}


listH34 = document.querySelectorAll("h3,h4")

for (var i = 0; i < listH34.length; i++) {
  var cc = listH34[i];
  var nextSib = cc.nextElementSibling;
  if(nextSib !== null && (nextSib.tagName == "BLOCKQUOTE" || nextSib.tagName == "PRE")){
	  cc.isEntry = true;
	  sib3 = nextSib.nextElementSibling;
	  var entryBox = document.createElement("div");
	  var shortDesc = document.createElement("span");
	  shortDesc.classList.add("shortEntryDesc")
	  entryBox.classList.add("entryBox");
      cc.parentElement.insertBefore(entryBox,cc);
	  entryBox.appendChild(cc);
	  entryBox.appendChild(shortDesc);
	  
	  var shortDescString = " &nbsp&nbsp&nbsp "+nextSib.textContent.split(".")[0].trim() + ".";
	  if( shortDescString.length > 100 ){
		  shortDescString = shortDescString.substr(0,100)+"...";
	  }
	  shortDesc.innerHTML = shortDescString;
	  //shortDesc.classList.add( "hiddenEntryContent" );
	  entryBox.shortDesc = shortDesc;
	  entryBox.appendChild(shortDesc);
	  
	  entryBox.appendChild(nextSib);
	  
	  entryBox.titleElem = cc;
	  entryBox.contentElemList = [nextSib];
	  nextSib.boxParent = cc;
	  cc.isTripleEntry = false;
	  entryBox.isTripleEntry = false;
	  cc.classList.toggle("inlineTitle")
	  
	  while(sib3 !== null && (sib3.tagName == "BLOCKQUOTE" || sib3.tagName == "PRE" || sib3.classList.contains("exampleBox"))){
		      var sib4 = sib3.nextElementSibling;
		  	  entryBox.appendChild(sib3);
			  cc.isTripleEntry = true;
			  entryBox.isTripleEntry = true;
			  entryBox.contentElemList.push(sib3);
			  sib3.boxParent = cc;
			  sib3 = sib4;
	  }
  } else {
	  cc.isEntry = false;
  }
}

listEB = document.querySelectorAll(".entryBox")

//cc.contentElemList[0].textContent.split(".")[0].trim()

for (var i = 0; i < listEB.length; i++) {
  var cc = listEB[i];
  //cc.shortDesc.classList.toggle( "hiddenEntryContent" );
  for(var j=0; j < cc.contentElemList.length; j++){
	  var bb = cc.contentElemList[j];
      bb.classList.toggle("hiddenEntryContent");
  }

  cc.addEventListener("click", function() {
    for(var j=0; j < this.contentElemList.length; j++){
      var bb = this.contentElemList[j];
	  bb.classList.toggle("hiddenEntryContent")
	}
	this.shortDesc.classList.toggle("hiddenEntryContent")	
	this.titleElem.classList.toggle("inlineTitle")
  })


}
console.log("DONE SCRIPT.JS");

var TOC = document.createElement("div");
TOC.classList.add("toc");

var tocInner = document.createElement("div");
tocInner.classList.add("tocInner");

TOC.appendChild(tocInner);

for (var i = 0; i < coll.length; i++) {
  var cc = coll[i];
  cc.id = cc.textContent;

  var tocElement = document.createElement("a");
  tocElement.classList.add("tocElement");
  tocElement.textContent = cc.id;
  tocElement.href = "#"+cc.id;
  tocInner.appendChild(tocElement);
  tocInner.appendChild(document.createElement("br"))
}

document.getElementsByClassName("wrapper")[0].prepend(TOC);


