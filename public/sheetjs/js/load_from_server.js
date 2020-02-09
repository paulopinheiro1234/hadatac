/* oss.sheetjs.com (C) 2014-present SheetJS -- http://sheetjs.com */
/* vim: set ts=2: */

var demo_enabled = false;
var sdd_suggestions;
var sddgenAdress;

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
function getURL(url){
  dd_url = url;
  // Get suggestions
  getSuggestion();
}



function hideView(){
  $("#hide").css('display','none');
  $(".mobile-nav").fadeOut(50);
  $("#show").show();
  cdg.style.height = '100%';
  cdg.style.width = '100%';

}

function changeHeader(headers,emptySheet){
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
    // if(s=="Dictionary Mapping Summary"){
    //   btn.style.width='120px';
    // }
    btn.innerHTML = s;
    // var txt = document.createElement('h6');
    // txt.innerText = s;
    // btn.appendChild(txt);

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

cellEntry.addEventListener('input', function (evt){
   if(textCell != null){
      cdg.data[textCell.rowIndex][textCell.columnIndex] = cellEntry.value;
   }
});

cellEntry.addEventListener('keyup', function (e){
   if(e.which==13||e.keyCode==13){
      cdg.data[textCell.rowIndex][textCell.columnIndex] = cellEntry.value;
      cellEntry.blur();
   }
});

cdg.addEventListener('click', function (e) {
  returnToView();
  colNum=e.cell.columnIndex;
  rowNum=e.cell.rowIndex;

  textCell = e.cell;

  var colNum_str=colNum.toString();
  var rowNum_str=rowNum.toString();
  if (!e.cell) { return; }

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
    // spinnerStatus.start();
    // imageStatus.style.visibility = 'hidden';
    if(demo_enabled){
      jsonparser(colval,rowval,menuoptns,isVirtual);
    }
    else{

      // Check if we have gotten recomendations yet
      if (typeof sdd_suggestions == 'undefined') {
         // Get suggestions
         // getSuggestion();
         // alert('Requesting Suggestions');
      }
      else{
         applySuggestion(colval,rowval,menuoptns,isVirtual);
      }
    }
  }
});

cdg.addEventListener('endedit',function(e){
  if (!e.cell) { return; }


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
  colNum=e.cell.columnIndex;
  rowNum=e.cell.rowIndex;
  cellEntry.value = cdg.data[rowNum][colNum];
  var colNum_str=colNum.toString();
  var rowNum_str=rowNum.toString();
  storeThisEdit(rowNum_str,colNum_str,e.value);
  var menuoptns=[];
  starRec(colval,rowval,menuoptns,isVirtual,copyOfL,copyOfR,rowNum,colNum);
})

cdg.addEventListener('click', function (e) {
  returnToView();
  if (!e.cell) { return; }
  if(e.cell.value==null){console.log("non");return;}
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
         for (const sddCol of sddRow[colval]){
            menuoptns.push([sddCol.star, sddCol.value]);
         }
         break; // After we find the correct value we can quit searching
      }
   }

   menuoptns=menuoptns.sort(sortByStar);
   createNewMenu(menuoptns,colval,isVirtual);
}



function chooseItem(data) {
  var choice=data.value.split(",");
  cdg.data[rowNum][colNum] = choice[1];
  var colNum_str=colNum.toString();
  var rowNum_str=rowNum.toString();
  fromSuggestionstoLabel(choice[1],rowNum+1,colNum);
  storeThisEdit(rowNum_str,colNum_str,cdg.data[rowNum][colNum]);
  drawStars(rowNum,colNum);
  cdg.draw();
  

}
cdg.addEventListener('contextmenu', function (e) {
  e.items.push({
      title: 'Insert Row Above',
      click: function (ev) {
          var intendedRow=e.cell.rowIndex;
          cdg.insertRow([],intendedRow);

      }
  });
  e.items.push({
    title: 'Insert Row Below',
    click: function (ev) {
        var intendedRow=parseFloat(e.cell.rowIndex);
        cdg.insertRow([],intendedRow+1);

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
            type : 'GET',
            url : 'http://localhost:9000/hadatac/sddeditor_v2/removingRow',
            data : {
              removedValue:temp[i]
            },
            success : function(data) {

            }
          });
        }


  storeRow.push(temp);
  var intendedRow=parseFloat(e.cell.rowIndex);
  cdg.deleteRow(intendedRow);

    }
  });
});
function insertRowAbove(){
  var intendedRow=parseFloat(rowNum);
  cdg.insertRow([],intendedRow); // The first argument splices a js array into the csv data, so to insert a blank row insert an empty array
}

function insertRowBelow(){
  var intendedRow=parseFloat(rowNum);
  cdg.insertRow([],intendedRow+1); // The first argument splices a js array into the csv data, so to insert a blank row insert an empty array
}

var storeRow=[];
function removeRow(){
  // alert("Warning! You are about to delete a row.");
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
      url : 'http://localhost:9000/hadatac/sddeditor_v2/removingRow',
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


}
function _resize() {
  _grid.style.height = (window.innerHeight - 300) + "px";
  _grid.style.width = '100%';
  
 
}
 _resize();

window.addEventListener('resize', _resize);


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
function gotothisfunction(sheetCopy){
  for(var i=0;i<sheetCopy.length;i++){
    var temp=[];
    for(var j=0;j<sheetCopy[i].length;j++){
      temp.push(sheetCopy[i][j]);
    }
    sheetStorage.push(temp);
  }
  console.log(sheetStorage);
  
 

}
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
  var R=0;
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
  
  if(sheetName=="Dictionary Mapping"){
    var sheetCopy=cleanJson
    gotothisfunction(sheetCopy);
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
  }
  else{
    headerMap.set(sheetName, json[0]);

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
  }

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

function getSuggestion(){
   spinnerStatus.stop()

   spinnerStatus = new Spinner(spinnerOpts).spin(spinnerTarget);
   imageStatus.style.visibility = 'hidden';

   var getJSON = function(url, callback) {

      // Generate data dictionary map be reading DD in
      var dataDictionary = []
      var oReq = new XMLHttpRequest();
      oReq.open("GET", dd_url, true);
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
            console.log(cdg.data)
            for (i = 0; i < cdg.data.length; i++) {
               if(!columnsAdded.includes(cdg.data[i][0])){
                  // console.log(cdg.data[i][0])

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

         // cdg.data[rowNum][colNum]

         // Get the ontologies for the Suggestion Request
         var ontRequest = new XMLHttpRequest();
         ontRequest.open('GET', 'http://localhost:9000/hadatac/sddeditor_v2/getOntologies', true);
         ontRequest.responseType = 'json';

         ontRequest.onload = function(e) {

            // Get the ontologies for the Suggestion Request
            var ontologyList = [];
            var ontologies = ontRequest.response;
            ontologies.forEach(function (item, index) {
              ontologyList.push(item['uri'])
           });
           console.log(ontologyList)


           // Generating Suggestion Request
          var request = {}
          request["source-urls"] = ontologyList
          // request["source-urls"] = ["http://semanticscience.org/resource/"];
          request["N"] = 4;
          request["data-dictionary"] = dataDictionary
          console.log(request);

          var xhr = new XMLHttpRequest();
          xhr.open("POST", url);
          xhr.setRequestHeader("Content-Type", "application/json")
          xhr.setRequestHeader("cache-control", "no-cache");
          xhr.responseType = 'json';
          xhr.onload = function() {
              var status = xhr.status;
              if (status == 200) {
                  callback(null, xhr.response);
              } else {
                  callback(status, xhr.response);
              }
          };

          xhr.addEventListener("error", function() {
            console.log('Couldnt connect to SDDgen');
            spinnerStatus.stop();
            imageStatus.style.visibility = 'visible';
            imageStatus.src = imgPath + 'fail.png'
          });

          xhr.send(JSON.stringify(request));
         }
         ontRequest.send();
      }
      oReq.send();
   };

   var sddGenFunction = function(err, data) {
      if (err != null) {
         console.error(err);
         console.log(data);
         spinnerStatus.stop();
         imageStatus.style.visibility = 'visible';
         imageStatus.src = imgPath + 'fail.png'

         if(err == 400){
            alert("Error: SDDGen is " + data['Bad Request'] + ': ' + data['Miss']);
         }

      }
      else {
         sdd_suggestions = data
         spinnerStatus.stop();
         imageStatus.style.visibility = 'visible';
         imageStatus.src = imgPath + 'success.png'
      }
   };

   if(sddgenAdress == null){
      var getSDDGenRequest = new XMLHttpRequest();
      getSDDGenRequest.open("GET", 'http://localhost:9000/hadatac/sddeditor_v2/getSDDGenAddress', true);
      getSDDGenRequest.responseType = 'json';
      getSDDGenRequest.onload = function(e) {
         sddgenAdress = getSDDGenRequest.response;
         console.log(sddgenAdress)
         getJSON(sddgenAdress + '/populate-sdd',  sddGenFunction);
      }
      getSDDGenRequest.send();
   }
   else{
      getJSON(sddgenAdress + '/populate-sdd',  sddGenFunction);
   }
}

function jsonparser(colval,rowval,menuoptns,isVirtual){
  var getJSON = function(url, callback) {
  var xhr = new XMLHttpRequest();
  xhr.open('GET', url, true);
  xhr.responseType = 'json';
  xhr.onload = function() {
      var status = xhr.status;
      if (status == 200) {
          spinnerStatus.stop();
          imageStatus.style.visibility = 'visible';
          imageStatus.src = imgPath + 'success.png';
          callback(null, xhr.response);
      } else {
          callback(status);
          spinnerStatus.stop();
          imageStatus.style.visibility = 'visible';
          imageStatus.src = imgPath + 'fail.png';
      }
  };

  xhr.onerror = function() {
      spinnerStatus.stop();
      imageStatus.style.visibility = 'visible';
      imageStatus.src = imgPath + 'fail.png'
  };

  xhr.send();
  };


  // getJSON('http://128.113.106.57:5000/get-sdd/',  function(err, data) {
  getJSON('http://localhost:5000/populate-sdd/',  function(err, data) {
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

function addOptionsToMenu(menuoptns,select){
  for(var i=0;i<menuoptns.length;i++){
    if(menuoptns[i]!=','){
      var opt=menuoptns[i];
      var optns=document.createElement("option")
      optns.textContent=opt;
      optns.value=opt;
      select.appendChild(optns);
    }
}
}
function createNewMenu(menuoptns,colval,isVirtual){
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
   oReq.open("GET", dd_url, true);
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
   oReq.open("GET", durl, true);
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


    if("Dictionary Mapping"==sheetName){

      var popElement=document.getElementById("populatesdd");
      popElement.removeAttribute("disabled");
       populateThis(headersCol);
      popElement.setAttribute("disabled", "disabled");
    }
    else if(sheetName!="Dictionary Mapping"){
      var popElement=document.getElementById("populatesdd");
      popElement.setAttribute("disabled", "disabled");
    }
  }

  oReq.send();

}

function populateThis(headersCol){
  var ct=-1;
  for(var i=0;i<headersCol.length;i++){
    cdg.insertRow([],i);
    ct++;
    console.log(headersCol[i]);
    cdg.data[ct][0]=headersCol[i];
  }
}
