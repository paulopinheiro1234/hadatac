
function checkRecs (L,R,checker){
    copyOfL=L;
    copyOfR=R;
    var cellName;
    var colIndex=0;
    var rowIndex=0;
    var isVirtual=0;
    for (var i=0;i<R;i++){
      for(var j=1;j<L;j++){
        //cdg.data[i][j]=cdg.data[i][j]+" * ";
        rowIndex=i;
        colIndex=j;
        var colval=cdg.schema[colIndex].title;
        colval=colval.charAt(0).toLowerCase() + colval.slice(1);
        var rowval=cdg.data[rowIndex][0];

        if(checker==1){
          if(colval=="Attribute"||colval=="Role"||colval=="Unit"||colval=="attribute"){
            isVirtual=0;
          }
          else if(colval=="attributeOf"||colval=="Time"||colval=="inRelationTo"||colval=="wasDerivedFrom"||colval=="wasGeneratedBy"
          || colval=="Relation"||colval=="Entity"){
            isVirtual=1;
          }
          var menuoptns=[];
          starRec(colval,rowval,menuoptns,isVirtual,L,R,rowIndex,colIndex);

        }
        else{
          //str.replace('a', '');
          if(cdg.data[i][j]!=null){
            cdg.data[i+1][j]=cdg.data[i][j].replace(' * ','');
          }
        }

    }

  }

}

function starRec(colval, rowval, menuoptns, isVirtual, L, R, rowIndex, colIndex){
	if (typeof sdd_suggestions != 'undefined') {
      if(rowval.startsWith("??")){
         var keyword="virtual-columns";
         helperStarRec(keyword, rowval, colval, sdd_suggestions, menuoptns, isVirtual, L, R, rowIndex, colIndex);
      }
      else{
         var keyword="columns";
         helperStarRec(keyword, rowval, colval, sdd_suggestions, menuoptns, isVirtual, L, R, rowIndex, colIndex);
      }
   }
}


var isSuggestion=0;
function helperStarRec(keyword, rowval, colval, data, menuoptns, isVirtual, L, R, rowIndex, colIndex){
   var virtualarray=Object.keys(data["sdd"]["Dictionary Mapping"][keyword]);
   var index=0;
   var checkcolval="";
   for (var i =0;i<data["sdd"]["Dictionary Mapping"][keyword].length;i++){
      if(data["sdd"]["Dictionary Mapping"][keyword][i]["column"]==rowval){
         index=i;
      }
   }

   if(index < data["sdd"]["Dictionary Mapping"][keyword].length){
      var tempcolarray = Object.keys(data["sdd"]["Dictionary Mapping"][keyword][index]);
      for (var m=0;m<tempcolarray.length;m++){
         if(tempcolarray[m] == colval){
            checkcolval = tempcolarray[m];
            if(data["sdd"]["Dictionary Mapping"][keyword][index]["column"]==rowval && colval==checkcolval){
               for(var n=0;n<data["sdd"]["Dictionary Mapping"][keyword][index][colval].length;n++){
                  var temp=[];
                  temp.push(data["sdd"]["Dictionary Mapping"][keyword][index][colval][n].star);
                  temp.push(data["sdd"]["Dictionary Mapping"][keyword][index][colval][n].value);
                  menuoptns.push(temp);
               }
            }

            if(menuoptns.length>0){
              isSuggestion=1
              drawStars(rowIndex,colIndex,isSuggestion,menuoptns);
            }
            break; // leave for loop early
         }
      }
   }
}

function drawStars(rowIndex,colIndex,isSuggestion,menuoptns){
    if(isSuggestion==0){

    }
    else{
      //cdg.data[rowIndex][colIndex]+=" * ";
      //cdg.draw();
      if(sheetName=="Dictionary Mapping"){
        drawCheck(rowIndex,colIndex);
        autoPopulateSDD(menuoptns,rowIndex,colIndex);
      }
    }



}


function getUri(link){
   // Check to see if we have a defined prefix
   var finalPrefix = "";
   var finalIriSub = "";
   for(var iriSub in prefixD) {
      // Check for matches
      if(link.includes(iriSub)){
         // Check to see if theres a better match
         if(iriSub.length > finalIriSub.length){
            finalIriSub = iriSub;
            finalPrefix = prefixD[iriSub];
         }
      }
   }

   // No matches so return empty string, is this the best idea?
   if(finalIriSub.length == 0){
      return finalIriSub;
   }

   // We have a match so convert to prefix form
   return link.replace(finalIriSub, finalPrefix + ":");
}


// This function is depricated try to use convertIRItoPrefix instead
// old code expects prefix to end with # or /
// While this is good practice as the HHEAR ontology proves people can choose others
// In addition it doesn't prefer the larger substitutions which would better for users


function autoPopulateSDD(menuoptns,rowIndex,colIndex){
  menuoptns=menuoptns.sort(sortByStar);
  var topchoice=menuoptns[0][1];
  var prefixedIRI = getUri(topchoice);
  if(prefixedIRI===""){
    if(!topchoice.includes("#")){
      var replacement=topchoice.split("/").pop();
      replacement = replacement.replace(/\_/g, ':');
      cdg.data[rowIndex][colIndex] = replacement;
    }
  }
  else{
    cdg.data[rowIndex][colIndex] = prefixedIRI;
  }
  cdg.draw();
  storeAutoVal(topchoice,rowIndex,colIndex)
}

function storeAutoVal(topchoice,rowIndex,colIndex){


    sheetStorage[rowIndex][colIndex]=cdg.data[rowIndex][colIndex];
    var finalLab=convertToLabel(topchoice);
  sheetStorage[rowIndex][colIndex]=finalLab




  // sheetStorageCopy[rowIndex][colIndex]=topchoice;



}
function convertshortToIri(Uri){
  var prefix;
  var suffix;
  var ret = 0;
  if(Uri.includes(":")){
    suffix=Uri.split(":").pop();
    prefix=Uri.slice(0, Uri.lastIndexOf(':'));
    for(var propName in prefixD) {
      if(prefixD.hasOwnProperty(propName)) {
          var propValue = prefixD[propName];
          if(propValue==prefix){
            ret=propName;
          }
      }
    }

}

if(ret == 0){
   return "unknown";
}
else{
   ret=ret+suffix;
}

return ret;
}


function stripStars(){

  //str.replace('a', '');
  checkRecs(copyOfL,copyOfR,0);
}
function getLink(link){
  var ret;
  if(link.startsWith("http")){
    ret=link;
  }
  else{
    ret="No link to show";
  }
  return ret;
}
function getDescription(cval){

  console.log(cval)
  var cellVal=cval.trim();
  var ret;
      $.ajax({
        type : 'POST',
        url : 'http://localhost:9000/hadatac/sddeditor_v2/getDescriptionFromIri',
        data : {
          iricode:cellVal
        },
        async: false,
        success : function(data) {
          console.log(data);
         ret=data
        }
      });
      return ret;
}
function drawCheck(rowIndex,colIndex){

  var imgs={};
  cdg.addEventListener('rendertext', function (e) {
    if (e.cell.rowIndex > -1 && sheetName==="Dictionary Mapping") {
        if (e.cell.rowIndex === rowIndex && e.cell.columnIndex===colIndex) {
            e.cell.formattedValue = e.cell.value ? '' : 'No Image';
        }
    }
  });
  var d='https://www.starfall.com/h/_images/green-corner-triangle.png'
  cdg.addEventListener('afterrendercell', function (e) {

    var i, contextGrid = this;
    if (sheetName==="Dictionary Mapping"&& e.cell.rowIndex === rowIndex && e.cell.columnIndex===colIndex
             && e.cell.rowIndex > -1) {
        // if we haven't already made an image for this, do it now
        if (!imgs[d]) {
            // create a new image object and store it in the imgs object
            i = imgs[d] = new Image();
            // get the image path from the cell's value
            i.src = d;
            // when the image finally loads
            // call draw() again to run the else path
            i.onload = function (parentNode) {
                contextGrid.draw();
            };
            return;
        }
        // if we have an image already, draw it.
        i = imgs[d];
        if (i.width !== 0) {
            i.targetHeight = e.cell.height/2;
            i.targetWidth = (e.cell.height * (i.width / i.height))/2;

            e.ctx.drawImage(i, e.cell.x, e.cell.y, i.targetWidth, i.targetHeight);
        }
    }
  });
}




addcartlocal()
function addcartlocal(){
  clearCart();
    $.ajax({
      type : 'POST',
      url : 'http://localhost:9000/hadatac/sddeditor_v2/getCart',
      data : {
        //  s: str
        
      },
      success : function(data) {
    
        //console.log(data)

    var select=document.getElementById("seecart"),data;
    for(var i=0;i<data.length;i++){
        var li = document.createElement("li");
        li.appendChild(document.createTextNode(data[i]));
        li.setAttribute("class","inCart");
        select.appendChild(li);
        li.addEventListener("click",function(e){

          var newOntology=e.target.innerHTML;
          var ret=getUri(newOntology);
 
          
          if(ret!=""){
            cdg.data[rowNum][colNum] = ret;
            var label=convertToLabel(ret);
            sheetStorage[rowNum][colNum]=label;
          }
          else{
            
              ret=newOntology;
              
              cdg.data[rowNum][colNum] = ret;
              sheetStorage[rowNum][colNum]=ret;
            
          }
          

           //cdg.data[rowNum][colNum]=part1;
           
           //fromCarttoLabel();

          var colNum_str=colNum.toString();
          var rowNum_str=rowNum.toString();
          storeThisEdit(rowNum_str,colNum_str,cdg.data[rowNum][colNum]);
          cdg.draw();
        })
        li.addEventListener("onmouseover",function(e){
          console.log("hi")
          li.setAttribute("style", "background-color:green;")
        })

      }
    }




});
}
function clearCart(){
  var ul = document.querySelector('.cart-content');
  var listLength = ul.children.length;

  for (i = 1; i < listLength; i++) {
    ul.removeChild(ul.children[0]);
  }
}

function storeThisEdit(rowNum_str,colNum_str,changeValue){
  $.ajax({
    type : 'GET',
    url : 'http://localhost:9000/hadatac/sddeditor_v2/addToEdits',
    data : {
      row: rowNum_str,
      col:colNum_str,
      editValue: changeValue
    },
    success : function(data) {
    	if(sheetName == "Dictionary Mapping") { 
    		getEditValue(parseInt(rowNum_str), parseInt(colNum_str), 1, changeValue);
    	}
    }
  });
}

function undoEdit(){
  $.ajax({
    type : 'POST',
    url : 'http://localhost:9000/hadatac/sddeditor_v2/getEdit',
    data : {
       //editValue: changeValue
    },
    success : function(data) {
      var rnum=Number(data[0]);
      var cnum=Number(data[1]);
      var valueRevert=data[2];
      cdg.data[rnum][cnum]=valueRevert;
      if(sheetName == "Dictionary Mapping") { 
    	  getEditValue(rnum, cnum, 1, valueRevert);
  	  }
      cdg.draw();
    }
  });
}

function reundoEdit(){
  $.ajax({
    type : 'POST',
    url : 'http://localhost:9000/hadatac/sddeditor_v2/getOldEdits',
    data : {
       //editValue: changeValue
    },
    success : function(data) {
      var rnum=Number(data[0]);
      var cnum=Number(data[1]);
      var valueRevert=data[2];
      cdg.data[rnum][cnum]=valueRevert;
      if(sheetName == "Dictionary Mapping") { 
    	  getEditValue(rnum, cnum, 1, valueRevert);
  	  }
      cdg.draw();

    }
  });
}

function revertRow(){

  var temp=[];
  for(var i=1;i<storeRow[storeRow.length-1].length;i++){
    temp.push(storeRow[storeRow.length-1][i]);
  }
  cdg.insertRow(temp,storeRow[storeRow.length-1][0]);
  storeRow.pop();

}





function indicateApproval(){
  for(var prop in approvalList){
    var r= approvalList[prop][0];
    var c= approvalList[prop][1];

    if(approvalList[prop][2]==0 && prop == cdg.data[r-1][c]){

      cdg.data[r-1][c]+=" + ";
      cdg.draw();

    }
  }

}
function acceptApproval(val,r,c){

  for(var prop in approvalList){

    if(approvalList[prop][2]==1){

      cdg.data[r][c]=val;

    }
  }

}
