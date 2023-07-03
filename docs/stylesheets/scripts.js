
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
	  var entryBoxTitle = document.createElement("div");
      var closexbox = document.createElement("div");
	  var shortDesc = document.createElement("span");
	  closexbox.classList.add("closexbox");

	  entryBoxTitle.classList.add("entryBoxTitle");
	  shortDesc.classList.add("shortEntryDesc")
	  entryBox.classList.add("entryBox");
      cc.parentElement.insertBefore(entryBox,cc);
      entryBoxTitle.appendChild(cc)
      entryBoxTitle.appendChild(closexbox);
	  entryBox.appendChild(entryBoxTitle);
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
      entryBox.classList.add("closedEntryBox")
      
	  entryBox.titleElem = cc;
	  entryBox.contentElemList = [closexbox,nextSib];
	  entryBox.closexbox = closexbox;
      closexbox.entryBox = entryBox;
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

  if( entryBox.closexbox == null ){
    cc.addEventListener("click", function() {
      for(var j=0; j < this.contentElemList.length; j++){
        var bb = this.contentElemList[j];
  	    bb.classList.toggle("hiddenEntryContent")
  	  }
	  this.shortDesc.classList.toggle("hiddenEntryContent")
	  this.titleElem.classList.toggle("inlineTitle")
      resizeAllCodeblocks();
    })
  } else {
    var ccx = cc.closexbox;

    cc.addEventListener("click", function( e ) {
      if( this.classList.contains( "closedEntryBox" ) ){
        for(var j=0; j < this.contentElemList.length; j++){
          var bb = this.contentElemList[j];
  	      bb.classList.toggle("hiddenEntryContent")
  	    }
	    this.shortDesc.classList.toggle("hiddenEntryContent")
	    this.titleElem.classList.toggle("inlineTitle")
        this.classList.toggle("closedEntryBox")
        resizeAllCodeblocks();
      }
      e.stopPropagation()
    })
    ccx.addEventListener("click", function( e ) {
        for(var j=0; j < this.entryBox.contentElemList.length; j++){
          var bb = this.entryBox.contentElemList[j];
  	      bb.classList.toggle("hiddenEntryContent")
  	    }
	    this.entryBox.shortDesc.classList.toggle("hiddenEntryContent")
	    this.entryBox.titleElem.classList.toggle("inlineTitle")
        this.entryBox.classList.toggle("closedEntryBox")
        this.entryBox.classList.add("TEST");
        resizeAllCodeblocks();
        e.stopPropagation()
    })
  }




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



var preblocks = document.getElementsByTagName('pre');

for( var i = 0; i < preblocks.length; i++){
    var pb = preblocks[i];
    var codeblocks = pb.getElementsByTagName("code");
    pb.codeblocks = codeblocks;
    for( var j = 0; j < codeblocks.length; j ++){
        var cb = codeblocks[j];
        cb.rawString = cb.textContent;
    }
}

function codeBreakLine(s,w){
    var spaceCt = s.search(/\S/);
    var breakIndent = " ".repeat(spaceCt + 8)
    if(spaceCt = -1){
       spaceCt = 0;
    }
    if(spaceCt + 16 >= w){
        console.log("Impossible fit: returning \""+s+"\"");
        return s;
    }
    if( s.length < w ){
        return s;
    }
    return codeBreakLineHelper(s,w,spaceCt,breakIndent,0);
}

var breakLineMaxDepth = 10;
function codeBreakLineHelper(s, w, spaceCt, breakIndent, depth){
    console.log("codeBreakLineHelper(\""+s+"\","+w+","+spaceCt+","+breakIndent.length+"s,"+depth+")")
    if(s.length <= w){
        return s;
    }
    if( breakLineMaxDepth <= depth ){
        //console.log("FAILED!");
        return s;
    }
    
    //console.log("attempting code linebreak["+depth+"] to width "+w+" \""+s+"\"")
    var prevbp = 0;
    var forceBreak = false;
    if( s.search(/\|/) < w && s.search(/\|/) != -1){
      //console.log("found BAR");
      prevbp = s.search(/\|/);
      while( prevbp < w){
         var xx = s.substring(prevbp+1).search(/\|/)
         var x = xx + prevbp + 1;
         //console.log("    Before:["+prevbp+"/"+xx+"/"+x+"/w="+w+"]");
         //prevbp = currbp;
         //currbp = x;
         //console.log("    After: ["+prevbp+"/"+currbp+"/"+x+"]");
         //if( xx == -1) break;
         if(xx == -1) break;
         if(x + 4 >= w) break;
         prevbp = x;
      }
      //console.log("BAR at idx: "+prevbp);
    } else if( s.substring( spaceCt+4 ).search(/ /) + spaceCt + 4 < w && s.substring( spaceCt+4 ).search(/ /) != -1 ) {
      //console.log("found SPACE");
      prevbp = s.substring( spaceCt+4 ).search(/ /) + spaceCt + 4
      while( prevbp < w ){
         var xx = s.substring(prevbp+1).search(/ /)
         var x = xx + prevbp + 1;
         //console.log("    Before:["+prevbp+"/"+xx+"/"+x+"/w="+w+"]");
         //prevbp = currbp;
         //currbp = x;
         //console.log("    After: ["+prevbp+"/"+currbp+"/"+x+"]");
         //if( xx == -1) break;
         if(xx == -1) break;
         if(x + 4 >= w) break;
         prevbp = x;
      }
      //console.log("SPACE at idx: "+prevbp);
    } else {
      //console.log("FORCE BREAK");
        forceBreak = true;
    }
    
    if(forceBreak || prevbp == 0){
        //console.log("out = \""+s.substring(0,w)+"\" + codeBreakLine(\""+breakIndent+s.substring(w)+"\","+w+")")
        return s.substring(0,w) + "\\\n" + codeBreakLineHelper(breakIndent + s.substring(w),w,breakIndent.length, breakIndent,depth+1)
    } else {
        //console.log("out = \""+s.substring(0,prevbp+1)+"\" + codeBreakLine(\""+breakIndent+s.substring(prevbp+1)+"\","+w+")")
        return s.substring(0,prevbp+1)+"\\\n"+codeBreakLineHelper(breakIndent+s.substring(prevbp+1),w,breakIndent.length,breakIndent,depth+1);
    }
}

var codeBlockResizeAttemptLimit = 10;

function resizeAllCodeblocks(){
    for( var i = 0; i < preblocks.length; i++){
        var pb = preblocks[i];
        var codeblocks = pb.getElementsByTagName("code");
        var rightSideX = pb.getBoundingClientRect().width + pb.getBoundingClientRect().x;
        pb.codeblocks = codeblocks;
        for( var j = 0; j < codeblocks.length; j ++){
            var cb = codeblocks[j];
            cb.textContent = cb.rawString;
            var cbx = cb.getBoundingClientRect().width + cb.getBoundingClientRect().x;
            var rawstr = cb.rawString;
            var lines = rawstr.split("\n");
            var attemptDedent = true;
            var attemptCt = 0;
            while(cbx > rightSideX & codeBlockResizeAttemptLimit > attemptCt){
                if(attemptDedent){
                    var indentLevels = [];
                    var lineIndentList = [];
                    for( k in lines ){
                        var line = lines[k];
                        var ind = line.search(/\S/);
                        lineIndentList.push(ind);
                        if( ( ind != -1 ) && ( indentLevels.indexOf(ind) == -1 )){
                            indentLevels.push( ind );
                        }
                        //if(cb.id == "initCode"){
                        //    console.log("line: \""+line+"\"");
                        //    console.log("     has indent: "+ind);
                        //}
                    }
                    indentLevels = indentLevels.sort( function(a,b){
                       return a-b;
                    });
                    //if(cb.id == "initCode"){
                    //    console.log("indentlevels = ");
                    //    console.log(indentLevels);
                    //}
                    var newIndent = [];
                    for( var k = 0; k < indentLevels.length; k++){
                        newIndent.push( " ".repeat(k*4) )
                        //if(cb.id == "initCode"){
                        //    console.log(newIndent);
                        //}
                    }
                    var outline = "";
                    for( var k = 0; k < lines.length; k++){
                        if( lineIndentList[k] != -1){
                            lines[k] = newIndent[ indentLevels.indexOf( lineIndentList[k] ) ] + lines[k].substr( lineIndentList[k] );
                        }
                        if( k > 0 ){ 
                           outline = outline +"\n";
                        }
                        outline = outline + lines[k];
                    }
                    cb.textContent = outline;
                    attemptDedent = false;
                } else {
                    var maxwidth = 0;
                    for( k in lines ){
                        var line = lines[k];
                        maxwidth = Math.max(maxwidth, line.length );
                    }
                    //var oversizeDiff = cbx - rightSideX
                    //var diffFract = Math.min(0.8,(cb.getBoundingClientRect().width / ( cb.getBoundingClientRect().width + oversizeDiff )) * 0.9)
                    //var maxLineLen = Math.floor( maxwidth * diffFract );
                    var tmp = cb.textContent;
                    var maxLineLen = maxwidth - 4;
                    cb.textContent = " ".repeat(maxLineLen);
                    var cbxt = cb.getBoundingClientRect().width + cb.getBoundingClientRect().x;
                    while( cbxt >= rightSideX ){
                        cb.textContent = " ".repeat(maxLineLen)
                        cbxt = cb.getBoundingClientRect().width + cb.getBoundingClientRect().x;
                        maxLineLen = maxLineLen - 4;
                    }
                    cb.textContent = tmp;
                    //console.log("attempting to fit line len:"+maxwidth+" to max line len: "+maxLineLen);
                    var out = "";
                    for( k in lines ){
                        var line = lines[k];
                        if(k > 0){ 
                           out=out+"\n" ;
                        }
                        //console.log("----------------------------------------------------------");
                        out = out + codeBreakLine(line,maxLineLen);
                    }
                    //console.log("out = \""+out+"\"")
                    var newMW = 0;
                    var newLines = out.split("\n");
                    for(k in newLines){
                        newMW = Math.max(newMW,newLines[k].length)
                    }
                    if( newMW < maxwidth ){
                       cb.textContent = out;
                       lines = out.split("\n");                                      
                    } else {
                       console.log("line wrap FAILED. break!");
                       cb.textContent = cb.rawString;
                       break;
                    }
                    
                    //console.log("OVERSIZED CODE BLOCK! TOO BIG TO DEDENT!");
                }
                //console.log("OVERSIZED CODE BLOCK!");
                attemptCt = attemptCt + 1;
                cbx = cb.getBoundingClientRect().width + cb.getBoundingClientRect().x;
            }
        }
    }
}


window.addEventListener('resize',function(){
   resizeAllCodeblocks()
});

resizeAllCodeblocks();




