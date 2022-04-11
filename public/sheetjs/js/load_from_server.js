var demo_enabled = false;
var sdd_suggestions;
var sdd_suggestions_organized;
var sddgenAdress;
var globalMenu;
var globalHeaders=[];
var popIndicator=0;

/** drop target **/
var _target = document.getElementById('drop');
var _file = document.getElementById('file');
var _grid = document.getElementById('grid');

var _gridcopy=document.getElementById("gridcopy");
var _buttons=document.getElementById("buttons");
var _footnote=document.getElementById("footnote");

/** Spinner **/
var spinner;

var _workstart = function() { spinner = new Spinner().spin(_target); }
var _workend = function() { spinner.stop(); }

// Load spinner
var spinnerOpts = {
   lines: 13, // The number of lines to draw
   length: 4, // The length of each line
   width: 2, // The line thickness
   radius: 6, // The radius of the inner circle
   corners: 1, // Corner roundness (0..1)
   rotate: 0, // The rotation offset
   color: '#00047c', // #rgb or #rrggbb
   speed: 1, // Rounds per second
   trail: 40, // Afterglow percentage
   shadow: false, // Whether to render a shadow
   hwaccel: false, // Whether to use hardware acceleration
   className: 'spinner', // The CSS class to assign to the spinner
   zIndex: 2e9
};
var spinnerTarget = document.getElementById('spinnerContainer');
var spinnerStatus = new Spinner(spinnerOpts).spin(spinnerTarget);

var imageStatus = document.getElementById('imageStatus');
imageStatus.style.visibility = 'hidden';

var imgPath = imageStatus.src.substr(0, imageStatus.src.length-11)

/** Alerts **/
var _badfile = function() {
   alertify.alert('This file does not appear to be a valid Excel file.  If we made a mistake, please send this file to <a href="mailto:dev@sheetjs.com?subject=I+broke+your+stuff">dev@sheetjs.com</a> so we can take a look.', function(){});
};

var _pending = function() {
   alertify.alert('Please wait until the current file is processed.', function(){});
};

var _large = function(len, cb) {
   alertify.confirm("This file is " + len + " bytes and may take a few moments.  Your browser may lock up during this process.  Shall we play?", cb);
};

var _failed = function(e) {
   console.log(e, e.stack);
   alertify.alert('We unfortunately dropped the ball here.  Please test the file using the <a href="/js-xlsx/">raw parser</a>.  If there are issues with the file processor, please send this file to <a href="mailto:dev@sheetjs.com?subject=I+broke+your+stuff">dev@sheetjs.com</a> so we can make things right.', function(){});
};

var dd_url;
var sdd_url
function getSDDUrl(url){
   sdd_url=url;
}
function getURL(url){
   dd_url = url;
   getSuggestion();
}

// retrieves indicator for whether the files have been downloaded already
$.ajax({
   type : 'POST',
   // url : 'http://localhost:9000/hadatac/sddeditor_v2/getIndicator',
   url : '/hadatac/sddeditor_v2/getIndicator',
   data : {

   },
   async: false,
   success : function(data) {
      if(data==1){
         popIndicator=1;
      }

   }
});

function hideView(){
   $("#hide").css('display','none');
   $(".mobile-nav").fadeOut(50);
   $("#show").show();
   cdg.style.height = '100%';
   cdg.style.width = '100%';

}

/* header editing */
function removeHeadings(json){
   json.splice(0,1);
   return json
}
function changeHeader(headers,emptySheet){
   globalHeaders=headers;
   if(emptySheet==0){
      for(var i = 0; i < headers.length; i++){
         cdg.schema[i].title = headers[i];

      }
      cdg.deleteRow(0);
   }
   else{
      for(var i = 0; i < headers.length; i++){
         cdg.schema[i].title = headers[i];
      }
   }
}

/* make the buttons for the sheets */
var sheetName;
var make_buttons = function(sheetnames, cb) {
   var buttons = document.getElementById('buttons');
   buttons.innerHTML = "";
   sheetnames.forEach(function(s,idx) {
      var btn = document.createElement('button');
      btn.style.height='40px';

      btn.style.display="inline-block";
      btn.style.padding="3px 3px 3px 3px"
      btn.style.fontSize='9pt';
      btn.style.fontFamily= 'Arial, Helvetica, sans-serif';
      btn.style.border="border:1px solid black";
      btn.style.marginTop="0px";
      btn.style.backgroundColor="lightgrey";
      btn.style.textAlign="center";
      btn.type = 'button';
      btn.name = 'btn' + idx;
      var canvas = document.createElement('canvas');
      var ctx = canvas.getContext("2d");
      ctx.font = "9pt Arial";
      var width = ctx.measureText(s).width;
      width+=10;
      btn.style.width=width;
      btn.innerHTML = s;


      btn.addEventListener('click', function() {
         sheetName=s;
         cb(idx);
         hideView();
         cdg.draw();
      }, false);
      buttons.appendChild(btn);
   });
   buttons.appendChild(document.createElement('br'));
};

// Creates the datagrid
var cdg = canvasDatagrid({
   parentNode: _grid
});

cdg.style.height = '100%';
cdg.style.width = '100%';

var colNum=0;
var rowNum=0;
var isVirtual=0;

var cellEntry = document.getElementById('cellText');

var textCell = null;
cellEntry.value = "";
//event listeners to edit and navigate the datagrid cells
cellEntry.addEventListener('input', function (evt){
   if(textCell != null){
      if(cdg.data[textCell.rowIndex][textCell.columnIndex] != cellEntry.value){
         approvalList[textCell.rowIndex][textCell.columnIndex] = "";
         indicateApproval(textCell.rowIndex, textCell.columnIndex, cellEntry.value);
      }
      cdg.data[textCell.rowIndex][textCell.columnIndex] = cellEntry.value;

   }
});

cellEntry.addEventListener('keyup', function (e){
   if(e.which==13||e.keyCode==13){
      cdg.data[textCell.rowIndex][textCell.columnIndex] = cellEntry.value;
      cellEntry.blur();
   }
});

//event listeners to navigate the datagrid
cdg.addEventListener('contextmenu', function (e) {
   if (!e.cell) { return; }

   if(approvalList[e.cell.rowIndex][e.cell.columnIndex] != ""){
      e.items.push({
         title: "Approved: " + approvalList[e.cell.rowIndex][e.cell.columnIndex]
      });
   }
});

var origVal;
cdg.addEventListener('click', function (e) {
   returnToView();
   colNum = e.cell.columnIndex;
   rowNum = e.cell.rowIndex;
   origVal = cdg.data[rowNum][colNum];
   textCell = e.cell;

   var colNum_str=colNum.toString();
   var rowNum_str=rowNum.toString();

   // Don't change anything if we are clicking on things that are not data cells
   if (!e.cell) { return; }
   if (e.cell.columnIndex < 0) { return; }
   if (e.cell.rowIndex < 0) { return; }

   if(e.cell.value==null){
      cdg.data[rowNum][colNum]=" ";
      cdg.draw();
      storeThisEdit(rowNum_str,colNum_str,cdg.data[rowNum][colNum]);
   }

   cellEntry.value = cdg.data[rowNum][colNum];

   storeThisEdit(rowNum_str,colNum_str,cdg.data[rowNum][colNum]);
   var colval=cdg.schema[e.cell.columnIndex].title;
   colval=colval.charAt(0).toLowerCase() + colval.slice(1);
   var rowval=cdg.data[e.cell.rowIndex][0];

   if(colval=="Attribute"||colval=="Role"||colval=="Unit"||colval=="attribute"){
      isVirtual=0;
   }
   else if(colval=="attributeOf"||colval=="Time"||colval=="inRelationTo"||colval=="wasDerivedFrom"||colval=="wasGeneratedBy"
   || colval=="Relation"||colval=="Entity"){
      isVirtual=1;
   }
   closeMenu(isVirtual);
   clearMenu(isVirtual);

   if(colNum==0){
      hideView();
   }

   else{
      var menuoptns=[];
      if(demo_enabled){
         jsonparser(colval,rowval,menuoptns,isVirtual);
      }
      else{

         // Check if we have gotten recomendations yet
         if (typeof sdd_suggestions == 'undefined') {
            // Get suggestions
            // getSuggestion();

         }
         else{
            applySuggestion(colval,rowval,menuoptns,isVirtual);
         }
      }
   }
});

//stores the edit to the datagrid
cdg.addEventListener('endedit',function(e){

   if (!e.cell) { return; }

   var rowval=cdg.data[e.cell.rowIndex][0];
   var colval=cdg.schema[e.cell.columnIndex].title;
   colval=colval.charAt(0).toLowerCase() + colval.slice(1);
   getEditValue(rowNum,colNum,1);

   if(colval=="Attribute"||colval=="Role"||colval=="Unit"||colval=="attribute"){
      isVirtual=0;
   }
   else if(colval=="attributeOf"||colval=="Time"||colval=="inRelationTo"||colval=="wasDerivedFrom"||colval=="wasGeneratedBy"
   || colval=="Relation"||colval=="Entity"){
      isVirtual=1;
   }
   colNum=e.cell.columnIndex;
   rowNum=e.cell.rowIndex;
   cellEntry.value = cdg.data[rowNum][colNum];
   var colNum_str=colNum.toString();
   var rowNum_str=rowNum.toString();
   storeThisEdit(rowNum_str,colNum_str,e.value);
   var menuoptns=[];
   starRec(colval,rowval,menuoptns,isVirtual,copyOfL,copyOfR,rowNum,colNum);
   if(approvalList[rowNum][colNum] != "" && origVal != cdg.data[rowNum][colNum]){
      approvalList[rowNum][colNum] = "";
      indicateApproval(rowNum,colNum,cdg.data[rowNum][colNum]);
   }

})

//set sheetStorage when Dictionary Mapping page is edited
function getEditValue(rowNum,colNum,ind,cellvalue){
   if(sheetStorage[rowNum][colNum] == null){
      sheetStorage[rowNum][colNum] = [];
   }
   var temp=[];
   if(ind==1){
      if(cdg.data[rowNum][colNum]!=""&&!cdg.data[rowNum][colNum].startsWith("??")&&cdg.data[rowNum][colNum].includes(":")){
         var lab = convertshortToIri(cdg.data[rowNum][colNum]);
         var finalLab;
         if("unknown" === lab){
            finalLab = "Unknown Ontology"
         }
         else{
            finalLab = convertToLabel(lab);
         }
         if(finalLab.includes("@")){
            finalLab = finalLab.slice(0, finalLab.indexOf("@"));
         }
         temp.push(finalLab);
      }
      else{
         temp.push(cdg.data[rowNum][colNum])
      }
      sheetStorage[rowNum][colNum] = temp;
   }
   else if(ind==0){
      if(cdg.data[rowNum][colNum]!=""&&!cdg.data[rowNum][colNum].startsWith("??")&&cdg.data[rowNum][colNum].includes(":")){
         var lab = convertshortToIri(cdg.data[rowNum][colNum]);
         var finalLab;
         if("unknown" === lab){
            finalLab = "Unknown Ontology"
         }
         else{
            finalLab = convertToLabel(lab);
         }
         if(finalLab.includes("@")){
            finalLab = finalLab.slice(0, finalLab.indexOf("@"));
         }
         temp.push(finalLab);
      }
      else{
         temp.push(cdg.data[rowNum][colNum])
      }
      sheetStorage[rowNum][colNum] = temp;
   }
}

//retrieves JSON format of the DD file
cdg.addEventListener('click', function (e) {
   returnToView();
   if (!e.cell) { return; }
   if(e.cell.value==null){ return; }
   else{
      colNum=e.cell.columnIndex;
      rowNum=e.cell.rowIndex;
      var varnameElement=cdg.data[rowNum][0];

      DDExceltoJSON(dd_url,varnameElement);
   }

});


function applySuggestion(colval, rowval, menuoptns, isVirtual) {
   var keyword="columns";
   if(rowval.startsWith("??")){
      keyword="virtual-columns";
   }

   for (const sddRow of sdd_suggestions["sdd"]["Dictionary Mapping"][keyword]){
      if(sddRow["column"] == rowval){
         if(colval in sddRow){
            for (const sddCol of sddRow[colval]){
               menuoptns.push([sddCol.star, sddCol.value]);
            }
            break; // After we find the correct value we can quit searching
         }
      }
   }

   menuoptns=menuoptns.sort(sortByStar);
   createNewMenu(menuoptns,colval,isVirtual);
}


//displays and formats prefixedIRI for the chosen data
function chooseItem(data) {
   var chosen=data.value.split(",")
   var prefixedIRI = getUri(chosen[1]);
   var hold = cdg.data[rowNum][colNum];
   if(prefixedIRI===""){
      if(!chosen[1].includes("#")){
         var replacement=chosen[1].split("/").pop();
         replacement = replacement.replace(/\_/g, ':');
         cdg.data[rowNum][colNum] = replacement;
      }
   }
   else{
      cdg.data[rowNum][colNum] = prefixedIRI;
   }


   var colNum_str=colNum.toString();
   var rowNum_str=rowNum.toString();
   storeThisEdit(rowNum_str,colNum_str,cdg.data[rowNum][colNum]);
   cdg.draw();

   if(hold != cdg.data[rowNum][colNum]){
      approvalList[rowNum][colNum] = "";
      indicateApproval(rowNum, colNum, cdg.data[rowNum][colNum]);
   }
}

//event listener for the context menu options
cdg.addEventListener('contextmenu', function (e) {

   e.items.push({
      title: 'Insert Row Above',
      click: function (ev) {
         var intendedRow=e.cell.rowIndex;
         cdg.insertRow([],intendedRow);
         createCopySheet(cdg.data);
         var copyApproval = [];
         for(var i=0; i<approvalList.length;i++){
            if(i == intendedRow){
               var colHold = [];
               for(var j = 0; j<approvalList[i].length; j++){
                  colHold.push("");
               }
               copyApproval[i] = colHold;
            }
            if(i < intendedRow){
               copyApproval[i] = approvalList[i];
            }
            if(i >= intendedRow){
               copyApproval[i+1] = approvalList[i];
            }
         }
         approvalList = copyApproval;
         for(var i=intendedRow; i<approvalList.length; i++){
            for(var j=0; j<approvalList[i].length; j++){
               // Not sure why we are checking for the cell under it, but I'm going to keep for reference
               if(approvalList[i][j] != ""){
               // if(approvalList[i][j] != "" || (approvalList[i+1][j] != null && approvalList[i+1][j]!="")){
                  indicateApproval(i,j,cdg.data[i][j]);
               }
            }
         }
      }
   });
   e.items.push({
      title: 'Insert Row Below',
      click: function (ev) {
         var intendedRow=parseFloat(e.cell.rowIndex);
         cdg.insertRow([],intendedRow+1);
         createCopySheet(cdg.data);
         var copyApproval = [];
         for(var i=0; i<approvalList.length;i++){
            if(i == intendedRow+1){
               var colHold = [];
               for(var j = 0; j<approvalList[i].length; j++){
                  colHold.push("");
               }
               copyApproval[i] = colHold;
            }
            if(i < intendedRow+1){
               copyApproval[i] = approvalList[i];
            }
            if(i >= intendedRow+1){
               copyApproval[i+1] = approvalList[i]
            }
         }
         approvalList = copyApproval;
         for(var i=intendedRow+1; i<approvalList.length; i++){
            for(var j=0; j<approvalList[i].length; j++){
               // Not sure why we are checking for the cell under it, but I'm going to keep for reference
               if(approvalList[i][j] != ""){
               // if(approvalList[i][j] != "" || (approvalList[i+1][j] != null && approvalList[i+1][j]!="")){
                  indicateApproval(i,j,cdg.data[i][j]);
               }
            }
         }
      }
   });
   e.items.push({
      title: 'Delete Row',
      click: function (ev) {

         var temp=[];
         temp.push(e.cell.rowIndex);
         for(var i=0;i<cdg.data[e.cell.rowIndex].length+1;i++){
            if(cdg.data[e.cell.rowIndex][i]==null){
               temp.push(" ");
            }
            else{
               temp.push(cdg.data[e.cell.rowIndex][i]);
            }
         }
         for( var i=1;i<temp.length;i++){
            $.ajax({
               type : "GET",
               // url : 'http://localhost:9000/hadatac/sddeditor_v2/removingRow',
               url : '/hadatac/sddeditor_v2/removingRow',
               data : {
                  removedValue: "test" //JSON.stringify(temp[i])
               },
               contentType: "application/json; charset=utf-8",
               dataType : "json",
               success : function(data) {
                  console.log("I worked!");
               }
            });
         }
         storeRow.push(temp);
         var intendedRow=parseFloat(e.cell.rowIndex);
         cdg.deleteRow(intendedRow);
         createCopySheet(cdg.data);
         storeApprovalRow=approvalList[intendedRow];
         approvalList[intendedRow] = [];
         for(var i=0; i<approvalList[0].length; i++){
            approvalList[intendedRow].push("");
         }
         for(var i=0; i<approvalList[intendedRow].length; i++){
            indicateApproval(intendedRow,i,cdg.data[intendedRow][i]);
         }
         approvalList.splice(intendedRow,1);
      }
   });
   if(approvalList[e.cell.rowIndex][e.cell.columnIndex]==""){
      e.items.push({
         title: 'Approve',
         click: function (ev) {
            var originalVal=e.cell.value;
            $.ajax({
               type : 'GET',
               // url : 'http://localhost:9000/hadatac/sddeditor_v2/getUserName',
               url : '/hadatac/sddeditor_v2/getUserName',
               data : {
                  email:email
               },
               success : function(nameData) {
                  var today = new Date();
                  var date = today.getFullYear()+'-'+(today.getMonth()+1)+'-'+today.getDate();
                  var time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
                  var dateTime = date+' '+time;
                  approvalList[e.cell.rowIndex][e.cell.columnIndex] = nameData + ' - ' + dateTime;
                  acceptApproval(originalVal,e.cell.rowIndex,e.cell.columnIndex);
                  indicateApproval(e.cell.rowIndex, e.cell.columnIndex, originalVal);
               }
            });
         }
      });
   }
});

/* functions for context menu */
function insertRowAbove(){
   var intendedRow=parseFloat(rowNum);
   cdg.insertRow([],intendedRow); // The first argument splices a js array into the csv data, so to insert a blank row insert an empty array
   copyApproval = [];
   for(var i=0; i<approvalList.length;i++){
      if(i == intendedRow){
         copyApproval[i] = []
      }
      if(i < intendedRow){
         copyApproval[i] = approvalList[i];
      }
      if(i >= intendedRow){
         copyApproval[i+1] = approvalList[i]
      }
   }
   approvalList = copyApproval;
   for(var i=0; i<approvalList.length; i++){
      for(var j=0; j<approvalList[i].length; j++){
         indicateApproval(i,j,cdg.data[i][j]);
      }
   }
}

function insertRowBelow(){
   var intendedRow=parseFloat(rowNum);
   cdg.insertRow([],intendedRow+1); // The first argument splices a js array into the csv data, so to insert a blank row insert an empty array
   copyApproval = [];
   for(var i=0; i<approvalList.length;i++){
      if(i == intendedRow+1){
         copyApproval[i] = []
      }
      if(i < intendedRow+1){
         copyApproval[i] = approvalList[i];
      }
      if(i >= intendedRow+1){
         copyApproval[i+1] = approvalList[i]
      }
   }
   approvalList = copyApproval;
   for(var i=0; i<approvalList.length; i++){
      for(var j=0; j<approvalList[i].length; j++){
         indicateApproval(i,j,cdg.data[i][j]);
      }
   }
}

var storeRow=[];
var storeApprovalRow=[];

// This seems to be duplicate code that is not run anymore
/*
function removeRow(){
   alert("Warning! You are about to delete a row.");
   var temp=[];
   temp.push(rowNum);
   for(var i=0;i<cdg.data[rowNum].length+1;i++){
      if(cdg.data[rowNum][i]==null){
         temp.push(" ");
      }
      else{
         temp.push(cdg.data[rowNum][i]);
      }
   }
   for( var i=1;i<temp.length;i++){
      $.ajax({
         type : 'GET',
         // url : 'http://localhost:9000/hadatac/sddeditor_v2/removingRow',
         url : '/hadatac/sddeditor_v2/removingRow',
         data : {
            removedValue:temp[i]
         },
         success : function(data) {

         }
      });
   }

   storeRow.push(temp);
   var intendedRow=parseFloat(rowNum);
   cdg.deleteRow(intendedRow);
   storeApprovalRow=approvalList[intendedRow];
   approvalList[intendedRow] = [];
   for(var i=0; i<approvalList[intendedRow].length; i++){
      indicateApproval(intendedRow,i,cdg.data[intendedRow][i]);
   }
   approvalList.splice(intendedRow,1);
}
*/

//resize
function _resize() {
   _grid.style.height = (window.innerHeight - 300) + "px";
   _grid.style.width = '100%';


}
_resize();

window.addEventListener('resize', _resize);

// variable declaration for data copying
var cdgcopy = canvasDatagrid({
   parentNode: _gridcopy
});
cdgcopy.style.height = '100%';

cdgcopy.style.width = '100%';


var click_ctr=0;
var copyOfL=0;
var copyOfR=0;

var headerMap = new Map();

var json_copy;

var sheetStorage=[];

// Adapted from to_json in dropsheet.js
function to_json(workbook) {
   var useworker = typeof Worker !== 'undefined';
   if(useworker && workbook.SSF) XLSX.SSF.load_table(workbook.SSF);
   var result = {};
   workbook.SheetNames.forEach(function(sheetName) {
      var roa = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName], {header:1});
      if(roa.length > 0) result[sheetName] = roa;
   });
   return result;
}

// Adapted from function handleFileUpload(e) in dropsheet.js
function saveFile(e){
   // Save the current view back to the workbook
   workbook.Sheets[sheetName] = XLSX.utils.aoa_to_sheet(cdg.data);

   // Generate json version of data
   var json = to_json(workbook);

   // Search for where the Approval Data is stored
   for(var i=0; i<json['InfoSheet'].length; i++){
      if(json['InfoSheet'][i][0] == "Approval Data"){
         json['InfoSheet'][i][1] = JSON.stringify(approvalList);
         break; // Only 1 row so leave early
      }
   }

   // iterate over workbook add headers in if we stripped them away
   for (let [sn, header] of headerMap) {
      // Add Header back in
      json[sn].unshift(header);

      // Convert back to workbook
      workbook.Sheets[sn] = XLSX.utils.aoa_to_sheet(json[sn]);
   }

   // Save the file
   var wbout = XLSX.write(workbook, { bookType: _filetype, bookSST: false, type: 'array', compression:true});

   var formdata = new FormData();
   if (_formData) {
      formdata.append('file', new File([wbout], _formData));
   }
   else {
      formdata.append('file', new File([wbout], 'sheetjs.' + filetype));
   }

   $.ajax({
      type : 'POST',
      url : _upload_url,
      data : formdata,
      contentType : false,
      processData : false,
      beforeSend : function() {
         _onreponse
      }
   });

   // iterate over workbook remove headers
   for (let [sn, header] of headerMap) {
      // Add Header back in
      json[sn].shift();

      // Convert back to workbook
      workbook.Sheets[sn] = XLSX.utils.aoa_to_sheet(json[sn]);
   }
}


document.getElementById('upload').addEventListener('click', saveFile, false);


function createCopySheet(sheetCopy){
   sheetStorage=[];

   for(var i=0;i<sheetCopy.length;i++){
      var temp=[];
      for(var j=0;j<sheetCopy[i].length;j++){
         if(sheetCopy[i][j]!=""&&!sheetCopy[i][j].startsWith("??")&&sheetCopy[i][j].includes(":")){
            var lab = convertshortToIri(sheetCopy[i][j]);
            var finalLab;
            if("unknown" === lab){
               finalLab = "Unknown Ontology"
            }
            else{
               finalLab = convertToLabel(lab);
            }
            if(finalLab.includes("@")){
               finalLab = finalLab.slice(0, finalLab.indexOf("@"));
            }
            temp.push(finalLab);
         }
         else{
            temp.push(sheetCopy[i][j])
         }
      }
      sheetStorage.push(temp);
   }
}

function approvalFunction(sheetCopy){
   if (approvalList === undefined || approvalList.length == 0) {
      // console.log("JSON.stringify(sheetCopy)");
      // console.log(JSON.stringify(sheetCopy));
      approvalList = JSON.parse(JSON.stringify(sheetCopy));
   }
   for(var i=0;i<sheetCopy.length;i++){

      for(var j=0;j<sheetCopy[i].length;j++){
         var keys=sheetCopy[i][j];
         var temp2=[];
         /*
         console.log(i);
         console.log(j);
         console.log("approvalList");
         console.log(approvalList);
         console.log("sheetCopy");
         console.log(sheetCopy);
         */
         if(approvalList[i][j] == sheetCopy[i][j]){
            approvalList[i][j] = "";
         }
         else{
            if(approvalList[i][j] != ""){
               indicateApproval(i,j,sheetCopy[i][j]);
            }
         }
      }
   }
}

var globalL;
var approvalList = []; // Stores the cells that have been approved
var _onsheet = function(json, sheetnames, select_sheet_cb) {

   document.getElementById('footnote').style.display = "none";
   click_ctr++;

   make_buttons(sheetnames, select_sheet_cb);

   /* show grid */
   _grid.style.display = "block";
   _resize();


   if (sheetName === undefined){ // sheetname will be undefined for the first sheet so set it to the default
      sheetName = sheetnames[0];
   }

   /* clean json */
   var L = 0;
   var R = 0;
   json.forEach(function(r) { if(L < r.length) L = r.length; }); // Gets the max width row

   var cleanJson = [];
   for(var i = 0; i < json.length; i++){
      if(json[i].length > 0){
         var temp = [];
         var j;
         for(j = 0; j < json[i].length; j++){
            if(typeof json[i][j] === 'string'){
               temp.push(json[i][j]);
            }
            else{
               temp.push(""); // replaces empty slots with a string
            }
         }
         while(j < L){
            temp.push("");
            j++;
         }
         cleanJson.push(temp)
      }
   }

   if(cleanJson.length == 1){ // We only have a header we need to add one blank row to avoid errors
      var temp = [];
      for(var j = 0; j < L; j++){
         temp.push("");
      }
      cleanJson.push(temp)
   }

   json = cleanJson;

   var emptySheet;
   /* set up table headers */
   if(headerMap.has(sheetName)){
      cdg.data = json;

      changeHeader(headerMap.get(sheetName),1);
      for(var i=0;i<cdg.data.length;i++){
         R++;
         checkRecs(L,R,1);
      }
      if(sheetName=="Dictionary Mapping"){
         var sheetCopy=json;
         createCopySheet(sheetCopy);

      }

   }
   else{
      headerMap.set(sheetName, json[0]);

      //Add Data Dictionary Link if it doesn't already exist
      if(sheetName=="InfoSheet"){
         var temp=[];
         var temp2=[];
         var DDLink = false;
         var ApproveData = false;
         for(var i=0;i<json.length;i++){
            if(json[i][0]=="Data Dictionary Link"){
               DDLink = true;
            }
            if(json[i][0]=="Approval Data"){
               ApproveData = true;

               if(json[i][1] == []){
                  ApproveData = false;
               }
               else{

                  // This adds bad approval starting data causing errors
                  // approvalList = JSON.parse(json[i][1]);
               }
            }
         }
         if(!DDLink){
            temp.push("Data Dictionary Link");
            temp.push(dd_url);
            json.push(temp)
         }
         if(!ApproveData){
            temp2.push("Approval Data");
            temp2.push(JSON.stringify(approvalList));
            json.push(temp2);
         }
      }
      if(sheetName=="Prefixes"){
         for(var i=0;i<newPrefix.length;i++){

            if(json[1][0]==""){
               json.splice(1,1)
            }
            json.push(newPrefix[i])
         }
      }


      if(json.length==1){
         cdg.data = json;
         emptySheet=0;
      }
      else{
         cdg.data = json.slice(1);
         for(var i=0;i<cdg.data.length;i++){
            R++;
            checkRecs(L,R,1);
         }


      }
      changeHeader(json[0], emptySheet);
      if(sheetName=="Dictionary Mapping"){
         var sheetCopy=removeHeadings(json);
         createCopySheet(sheetCopy);
         approvalFunction(sheetCopy);
         if(popIndicator==1 && cdg.data.length<=1){
            populateSDD();
         }
      }
   }

   globalL=L;
   checkRecs(L, cdg.data.length, 1);
   cdg.draw();
};

// setSheet

function parseJson_(keyword,rowval,colval,data,menuoptns,isVirtual){
   var virtualarray=Object.keys(data["sdd"]["Dictionary Mapping"][keyword]);
   var index=0;
   var checkcolval="";
   for (var i =0;i<data["sdd"]["Dictionary Mapping"][keyword].length;i++){
      if(data["sdd"]["Dictionary Mapping"][keyword][i]["column"]==rowval){
         index=i;
      }
   }
   var tempcolarray=Object.keys(data["sdd"]["Dictionary Mapping"][keyword][index]);
   for (var m=0;m<tempcolarray.length;m++){
      if(tempcolarray[m]==colval){
         checkcolval=tempcolarray[m];
      }
   }

   if(data["sdd"]["Dictionary Mapping"][keyword][index]["column"]==rowval && colval==checkcolval){
      for(var n=0;n<data["sdd"]["Dictionary Mapping"][keyword][index][colval].length;n++){
         var temp=[];
         temp.push(data["sdd"]["Dictionary Mapping"][keyword][index][colval][n].star);
         temp.push(data["sdd"]["Dictionary Mapping"][keyword][index][colval][n].value);
         menuoptns.push(temp);
      }

   }

   menuoptns=menuoptns.sort(sortByStar);
   createNewMenu(menuoptns,colval,isVirtual);
}

var labelsList=[];
var ontsList=[];
function getSuggestion(){
   spinnerStatus.stop()

   spinnerStatus = new Spinner(spinnerOpts).spin(spinnerTarget);
   imageStatus.style.visibility = 'hidden';

   var getJSON = function(url, callback) {

      // Generate data dictionary map be reading DD in
      var dataDictionary = []
      var oReq = new XMLHttpRequest();
      oReq.open("POST", dd_url, true);
      oReq.responseType = "arraybuffer";

      oReq.onload = function(e) {
         var arraybuffer = oReq.response;

         /* convert data to binary string */
         var data = new Uint8Array(arraybuffer);
         var arr = new Array();
         for (var i = 0; i != data.length; ++i) arr[i] = String.fromCharCode(data[i]);
         var bstr = arr.join("");

         /* Call XLSX */
         var workbook = XLSX.read(bstr, {
            type: "binary"
         });

         /* Get worksheet */
         var worksheet = workbook.Sheets[workbook.SheetNames[2]];
         var xlarray=XLSX.utils.sheet_to_json(worksheet, {
            raw: true
         });

         // Generate data dictionary
         columnsAdded = []
         for(var i=0; i<xlarray.length; i++){
            dataDictionary.push(
               {
                  "column": xlarray[i]['VARNAME '],
                  "description": xlarray[i]['VARDESC ']
               }
            );
            columnsAdded.push(xlarray[i]['VARNAME '])
         }

         if("Dictionary Mapping"==sheetName){

            for (i = 0; i < cdg.data.length; i++) {
               if(!columnsAdded.includes(cdg.data[i][0])){

                  dataDictionary.push(
                     {
                        "column": cdg.data[i][0],
                        "description": ""
                     }
                  );

                  columnsAdded.push(cdg.data[i][0])
               }
            }
         }
         else{
            console.log('Full Suggestions only work on the dictionary mapping page currently')
         }

         $.ajax({
            type : 'POST',
            // url : 'http://localhost:9000/hadatac/sddeditor_v2/getOntologiesKeys',
            url : '/hadatac/sddeditor_v2/getOntologiesKeys',
            dataType: 'json',
            success : function(labelRequest) {
               labelsList = labelRequest
            }
         });

         // Get the ontologies for the Suggestion Request
         $.ajax({
            type : 'POST',
            // url : 'http://localhost:9000/hadatac/sddeditor_v2/getOntologies',
            url : '/hadatac/sddeditor_v2/getOntologies',
            dataType: 'json',
            success : function(ontRequest) {
               var ontologyList = ontRequest;
               ontsList = ontologyList;
               SDDPrefixtoJSON();

               // Generating Suggestion Request
               var request = {}
               request["source-urls"] = ontologyList
               // request["source-urls"] = ["http://semanticscience.org/resource/"];
               request["N"] = 4;
               request["data-dictionary"] = dataDictionary

               $.ajax({
                  type : 'POST',
                  url : url,
                  data : JSON.stringify(request),
                  dataType : 'json',
                  contentType : "application/json",
                  cache : false,
                  success : function(data, stat, xhr) {
                     var status = xhr.status;
                     if (status == 200) {
                        callback(null, data);
                     } else {
                        callback(status, data);
                     }
                  },
                  error : function(xhr, textStatus, errorThrown) {
                     console.log('Couldnt connect to SDDgen');
                     // console.log("FAIL: " + xhr + " " + textStatus + " " + errorThrown);
                     console.log(xhr.responseJSON['Bad Request']);
                     if(xhr.responseJSON['Bad Request'] === 'ontologies must be not be empty'){
                        console.log('This error is usually caused by not loading namespace ontologies into the triple store');
                     }

                     spinnerStatus.stop();
                     imageStatus.style.visibility = 'visible';
                     imageStatus.src = imgPath + 'fail.png'
                  }
               });
            }
         });
      }
      oReq.send();

   };

   var sddGenFunction = function(err, data) {
      if (err != null) {
         console.error(err);
         console.log(data);
         spinnerStatus.stop();
         imageStatus.style.visibility = 'visible';
         imageStatus.src = imgPath + 'fail.png';

         if(err == 400){
            alert("Error: SDDGen is " + data['Bad Request'] + ': ' + data['Miss']);
         }

      }
      else {
         sdd_suggestions = data;
         // This data is duplicated but reorganized for quicker access at runtime
         // Someday we might completely sever the use of sdd_suggestions

         sdd_suggestions_organized = {};
         for(const i of sdd_suggestions.sdd["Dictionary Mapping"].columns){
            sdd_suggestions_organized[i.column] = {};
            sdd_suggestions_organized[i.column]['attribute'] = i.attribute;
            // TODO: We need to add the other SDD columns and virtual columns
         }

         spinnerStatus.stop();
         imageStatus.style.visibility = 'visible';
         imageStatus.src = imgPath + 'success.png'
      }
   };

   if(sddgenAdress == null){
      $.ajax({
         type : 'POST',
         // url : 'http://localhost:9000/hadatac/sddeditor_v2/getSDDGenAddress',
         url : '/hadatac/sddeditor_v2/getSDDGenAddress',
         dataType: 'json',
         success : function(getSDDGenRequest) {
            sddgenAdress = getSDDGenRequest;
            // Add trailing slash if its missing from the config file
            if (sddgenAdress.substr(-1) != '/') {         // If the last character is not a slash
               sddgenAdress = sddgenAdress + '/';            // Append a slash to it.
            }
            $.ajax({
               type : 'POST',
               url : sddgenAdress + 'ping',
               dataType : 'json',
               timeout : 5000,
               success : function(ping) {
                  // console.log(ping);
                  if(ping){
                     getJSON(sddgenAdress + 'populate-sdd',  sddGenFunction);
                  }
               },
               error : function() {
                  spinnerStatus.stop();
                  imageStatus.src = imgPath + 'fail.png';
                  imageStatus.style.visibility = 'visible';
                  alert("Error: SDDGen not running");
               }
            });
         }
      });
   }
   else{
      $.ajax({
         type : 'POST',
         url : sddgenAdress + 'ping',
         dataType : 'json',
         timeout : 5000,
         success : function(ping) {
            console.log(ping);
            if(ping){
               getJSON(sddgenAdress + 'populate-sdd',  sddGenFunction);
            }
         },
         error : function() {
            spinnerStatus.stop();
            imageStatus.src = imgPath + 'fail.png';
            imageStatus.style.visibility = 'visible';
            //imageStatus.src = imgPath + 'fail.png';
            alert("Error: SDDGen not running");
         }
      });
   }
   checkRecs(globalL, globalR, 1);
}

function jsonparser(colval,rowval,menuoptns,isVirtual){
   var getJSON = function(url, callback) {
      $.ajax({
         type : 'POST',
         url : url,
         dataType : "json",
         success : function(callbackData, status, xhr) {
            if (xhr.status == 200) {
               spinnerStatus.stop();
               imageStatus.style.visibility = 'visible';
               imageStatus.src = imgPath + 'success.png';
               callback(null, callbackData);
            } else {
               callback(xhr.status);
               spinnerStatus.stop();
               imageStatus.style.visibility = 'visible';
               imageStatus.src = imgPath + 'fail.png';
            }
         },
         error : function() {
            spinnerStatus.stop();
            imageStatus.style.visibility = 'visible';
            imageStatus.src = imgPath + 'fail.png'
         }
      });

   };


   getJSON(sddgenAdress + 'populate-sdd/',  function(err, data) {
      if (err != null) {
         console.error(err);
      }

      else {
         if(rowval.startsWith("??")){
            var keyword="virtual-columns";
            parseJson_(keyword,rowval,colval,data,menuoptns,isVirtual);
         }
         else{
            var keyword="columns";
            parseJson_(keyword,rowval,colval,data,menuoptns,isVirtual);
         }
      }
   });
}
var menuOptionsPrefixedIRI=[]
function addOptionsToMenu(menuoptns,select){
   for(var i=0;i<menuoptns.length;i++){
      if(menuoptns[i]!=','){
         var opt=menuoptns[i];
         var optns=document.createElement("option")

         // Making the suggestions pretty
         var suggBegin  = opt[0].toFixed(2) + "\xa0\xa0\xa0";

         // Check to see if we have a prefix for this suggestion
         var prefixedIRI = getUri(opt[1]);
         if(prefixedIRI === ""){
            // No prefix found so we don't have this ontology return IRI
            //ex HHEAR
            optns.textContent = suggBegin + opt[1];
         }
         else{
            // Check to see if we have a label for this suggestion
            var label = convertToLabel(opt[1]);
            if(label == null){
               // No label found so we don't have this label return prefix IRI
               optns.textContent = suggBegin + prefixedIRI;
            }
            else{
               // We have the label and prefix
               // Remove the language tags
               if(label.includes("@")){
                  label = label.split("@")[0];
               }

               // Get the ontolgoy prefix
               prefix = prefixedIRI.split(":")[0];
               optns.textContent = suggBegin + label + " (" + prefix + ")";
            }
         }

         optns.value=opt;
         select.appendChild(optns);
      }
   }
}

function createNewMenu(menuoptns,colval,isVirtual){
   globalMenu=menuoptns;

   if(isVirtual==0){
      var select=document.getElementById("menulist"),menuoptns;
      addOptionsToMenu(menuoptns,select);
      displayMenu(menuoptns.length,isVirtual);
   }
   else if(isVirtual==1){
      var select=document.getElementById("virtuallist"),menuoptns;
      addOptionsToMenu(menuoptns,select);
      displayMenu(menuoptns.length,isVirtual);
   }
}



function displayMenu(sizeOfMenu,isVirtual){
   if(sizeOfMenu>0 && isVirtual==0){
      var menu = document.getElementById("menulist");
      menu.style.display = "block";
      closeMenu(1);
   }
   else if (sizeOfMenu==0 && isVirtual==0){
      closeMenu(isVirtual);
   }
   else if(sizeOfMenu>0 && isVirtual==1){
      var menu = document.getElementById("virtuallist");
      menu.style.display = "block";
      closeMenu(0);
   }
   else if (sizeOfMenu==0 && isVirtual==1){
      closeMenu(isVirtual);
   }

}

function closeMenu(isVirtual){
   if(isVirtual==0){
      var menu = document.getElementById("menulist");
      menu.style.display = "none";
   }
   if(isVirtual==1){
      var menu = document.getElementById("virtuallist");
      menu.style.display = "none";
   }
}




function clearMenu(isVirtual){
   if(isVirtual==0){
      var selectbox=document.getElementById("menulist");
      if(selectbox.options==null){
      }
      else{
         for(var i = selectbox.options.length - 1 ; i > 0 ; i--){
            selectbox.remove(i);
         }

      }
   }
   else if(isVirtual==1){
      var selectbox=document.getElementById("virtuallist");
      if(selectbox.options==null){
      }
      else{
         for(var i = selectbox.options.length - 1 ; i > 0 ; i--){
            selectbox.remove(i);
         }

      }
   }
}


function sortByStar(a,b){
   if (a[0] === b[0]) {
      return 0;
   }
   else {
      return (a[0] > b[0]) ? -1 : 1;
   }
}


function clearTextbox(){
   document.getElementById("varDescription").value="";
}


function DDExceltoJSON(dd_url,varnameElement){

   var oReq = new XMLHttpRequest();
   oReq.open("POST", dd_url, true);
   oReq.responseType = "arraybuffer";

   oReq.onload = function(e) {
      var arraybuffer = oReq.response;

      /* convert data to binary string */
      var data = new Uint8Array(arraybuffer);
      var arr = new Array();
      for (var i = 0; i != data.length; ++i) arr[i] = String.fromCharCode(data[i]);
      var bstr = arr.join("");

      /* Call XLSX */
      var workbook = XLSX.read(bstr, {
         type: "binary"
      });

      var first_sheet_name = workbook.SheetNames[2];

      /* Get worksheet */
      var worksheet = workbook.Sheets[first_sheet_name];
      var xlarray=XLSX.utils.sheet_to_json(worksheet, {
         raw: true
      });
      var indx=0;
      clearTextbox();
      for(var i=0;i<xlarray.length;i++){
         if(xlarray[i]['VARNAME ']==varnameElement){
            indx=i;
            document.getElementById("varDescription").value=xlarray[indx]['VARDESC '];
         }
      }
   }
   oReq.send();
}


var closebtns = document.getElementsByClassName("remove");
var i;

for (i = 0; i < closebtns.length; i++) {
   closebtns[i].addEventListener("click", function() {
      this.parentElement.style.display = 'none';
   });
}


function DDforPopulate(durl,headersheet,headercol){

   var oReq = new XMLHttpRequest();
   oReq.open("POST", durl, true);
   oReq.responseType = "arraybuffer";

   oReq.onload = function(e) {
      var arraybuffer = oReq.response;

      /* convert data to binary string */
      var data = new Uint8Array(arraybuffer);
      var arr = new Array();
      for (var i = 0; i != data.length; ++i) arr[i] = String.fromCharCode(data[i]);
      var bstr = arr.join("");

      /* Call XLSX */
      var workbook = XLSX.read(bstr, {
         type: "binary"
      });


      var first_sheet_name = headersheet;
      /* Get worksheet */

      var worksheet = workbook.Sheets[first_sheet_name];
      var xlarray=XLSX.utils.sheet_to_json(worksheet, {
         raw: true
      });
      var headersCol=[];
      xlarray.forEach(function(item) {
         Object.keys(item).forEach(function(key) {
            if(key==headercol){
               headersCol.push(item[key]);
            }
         });
      });
      populateThis(headersCol)
   }

   oReq.send();

}
var globalR;
function populateThis(headersCol){
   var ct=-1;
   for(var i=0;i<headersCol.length;i++){
      cdg.insertRow([],i);
      ct++;

      cdg.data[ct][0]=headersCol[i];
   }
   globalR= cdg.data.length;
   createCopySheet(cdg.data);
   approvalList = undefined; // This is to reset the approval data as we import a new DD
   approvalFunction(cdg.data);
   getSuggestion();


}
var durl_;


function populateSDD(){

   $.ajax({
      type : 'POST',
      // url : 'http://localhost:9000/hadatac/sddeditor_v2/getHeaderLoc',
      url : '/hadatac/sddeditor_v2/getHeaderLoc',
      data : {

      },
      success : function(data) {

         getHeaderData(data);
      }
   });
   $.ajax({
      type : 'POST',
      // url : 'http://localhost:9000/hadatac/sddeditor_v2/getCommentLoc',
      url : '/hadatac/sddeditor_v2/getCommentLoc',
      data : {

      },
      success : function(data) {

         comments_data= data;

      }

   });
}


function getHeaderData(data){
   var headersheet=data.split('-')[0];
   var headercol=data.split('-')[1];
   DDforPopulate(durl_,headersheet,headercol)
}
