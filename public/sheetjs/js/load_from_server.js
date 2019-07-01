/* oss.sheetjs.com (C) 2014-present SheetJS -- http://sheetjs.com */
/* vim: set ts=2: */

/** drop target **/
var _target = document.getElementById('drop');
var _file = document.getElementById('file');
var _grid = document.getElementById('grid');

/** Spinner **/
var spinner;

var _workstart = function() { spinner = new Spinner().spin(_target); }
var _workend = function() { spinner.stop(); }

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
  dd_url=url;
}
function hideView(){
  $("#hide").css('display','none');
  $(".mobile-nav").fadeOut(50);
  $("#show").show();
  _grid.style.height = (window.innerHeight - 200) + "px";
  _grid.style.width = (window.innerWidth - 100) + "px";

}
/* make the buttons for the sheets */
var make_buttons = function(sheetnames, cb) {
  var buttons = document.getElementById('buttons');
  buttons.innerHTML = "";
  sheetnames.forEach(function(s,idx) {
    var btn = document.createElement('button');
    btn.type = 'button';
    btn.name = 'btn' + idx;
    btn.text = s;
    var txt = document.createElement('h5'); 
    txt.innerText = s; 
    btn.appendChild(txt);
    btn.addEventListener('click', function() { cb(idx); hideView();}, false);
    buttons.appendChild(btn);
  });
  buttons.appendChild(document.createElement('br'));
};

var cdg = canvasDatagrid({
  parentNode: _grid
});
cdg.style.height = '100%';
cdg.style.width = '100%';
var cellsClicked=[];
var colNum=0;
var rowNum=0;
cdg.addEventListener('click', function (e) {
  if (!e.cell) { return; }

  clearMenu();
  cellsClicked.push(e.cell.value);
  var colval=cdg.data[0][e.cell.columnIndex];
  colval=colval.charAt(0).toLowerCase() + colval.slice(1);
  var rowval=cdg.data[e.cell.rowIndex][0];

  colNum=e.cell.columnIndex;
  rowNum=e.cell.rowIndex;
  
  if((colNum==0 && rowNum!=0)){
    hideView();
  }
  else if((colNum!=0 && rowNum==0)||(colNum==0 && rowNum==0)){
    clearMenu();
    hideView();
    alert("The first row cannot be edited");

  }
  else{
    var menuoptns=[];
    jsonparser(colval,rowval,menuoptns);
  }
});


cdg.addEventListener('click', function (e) {
  if (!e.cell) { return; }
  else if(e.cell.columnIndex!=0 && e.cell.rowIndex!=0){clearTextbox();}
  
  else if((e.cell.columnIndex==0 && e.cell.rowIndex!=0)||(e.cell.columnIndex==0 && e.cell.rowIndex==0)){
    colNum=e.cell.columnIndex;
    rowNum=e.cell.rowIndex;
    var varnameElement=cdg.data[rowNum][0];
    
    DDExceltoJSON(dd_url,varnameElement);
  }

});


function chooseItem(data) {
  var choice=data.value.split(",");
  cdg.data[rowNum][colNum] = choice[1];
  cdg.draw();

}

function insertRowAbove(){
  console.log(rowNum);
  var intendedRow=parseFloat(rowNum);
  cdg.insertRow([],intendedRow); // The first argument splices a js array into the csv data, so to insert a blank row insert an empty array
}

function insertRowBelow(){
  var intendedRow=parseFloat(rowNum);
  cdg.insertRow([],intendedRow+1); // The first argument splices a js array into the csv data, so to insert a blank row insert an empty array
}

function removeRow(){
  var intendedRow=parseFloat(rowNum);
  cdg.deleteRow(intendedRow);
  

}
function _resize() {
  _grid.style.height = (window.innerHeight - 200) + "px";
  _grid.style.width = (window.innerWidth - 100) + "px";
}
_resize();
window.addEventListener('resize', _resize);

var _onsheet = function(json, sheetnames, select_sheet_cb) {
  document.getElementById('footnote').style.display = "none";

  make_buttons(sheetnames, select_sheet_cb);

  /* show grid */
  _grid.style.display = "block";
  _resize();

  /* set up table headers */
  var L = 0;
  json.forEach(function(r) { if(L < r.length) L = r.length; });
  // console.log(L);
  for(var i = json[0].length; i < L; ++i) {
    json[0][i] = "";
  }

  /* load data */
  cdg.data = json;
  for(var i=0;i<cdg.data[0].length;i++){
    cdg.schema[i].title = cdg.data[0][cdg.schema[i].name];
  }
  cdg.draw();
};

function jsonparser(colval,rowval,menuoptns){
  var getJSON = function(url, callback) {
  var xhr = new XMLHttpRequest();
  xhr.open('GET', url, true);
  xhr.responseType = 'json';
  xhr.onload = function() {
      var status = xhr.status;
      if (status == 200) {
          callback(null, xhr.response);
      } else {
          callback(status);
      }
  };

  xhr.send();
  };

  getJSON('http://128.113.106.57:5000/get-sdd/',  function(err, data) {
  if (err != null) {
      console.error(err);
  }
  else {
    var colvalarray=Object.keys(data["sdd"]["Dictionary Mapping"]["columns"]["0"]);
    var checkcolval;
    var index=0;
    for (var k=0;k<colvalarray.length;k++){
      colvalarray[k]=colvalarray[k].charAt(0).toLowerCase() + colvalarray[k].slice(1);
      if(colvalarray[k]==colval){
        checkcolval=colvalarray[k];
      }
    }
    for (var j =0;j<data["sdd"]["Dictionary Mapping"]["columns"].length;j++){
      if(data["sdd"]["Dictionary Mapping"]["columns"][j]["column"]==rowval){
        index=j;
      }
    }

    if(data["sdd"]["Dictionary Mapping"]["columns"][index]["column"]==rowval && colval==checkcolval){
      for(var i=0;i<data["sdd"]["Dictionary Mapping"]["columns"][index][colval].length;i++){
        var temp=[];
        temp.push(data["sdd"]["Dictionary Mapping"]["columns"][index][colval][i].confidence);
        temp.push(data["sdd"]["Dictionary Mapping"]["columns"][index][colval][i].value);
        menuoptns.push(temp);
      }
    }
    menuoptns=menuoptns.sort(sortByConfidence);
    createNewMenu(menuoptns);
    }
  });
}



function createNewMenu(menuoptns){

  var select=document.getElementById("menulist"),menuoptns;
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

function clearMenu(){
    var selectbox=document.getElementById("menulist");
    if(selectbox.options==null){
      console.log("nothing");
    }
    else{
      for(var i = selectbox.options.length - 1 ; i > 0 ; i--){
        selectbox.remove(i);
    }

    }
  }
  function sortByConfidence(a,b){
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

function showOntologyList(){
  document.getElementById("myDropdown").classList.toggle("show");
}

var closebtns = document.getElementsByClassName("remove");
  var i;
  
  for (i = 0; i < closebtns.length; i++) {
    closebtns[i].addEventListener("click", function() {
      this.parentElement.style.display = 'none';
    });
}

