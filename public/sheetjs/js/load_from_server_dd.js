/* oss.sheetjs.com (C) 2014-present SheetJS -- http://sheetjs.com */
/* vim: set ts=2: */

var demo_enabled = true;
var sdd_suggestions;

/** drop target **/
var _target = document.getElementById('drop');
var _file = document.getElementById('file');
var _grid = document.getElementById('grid');

var _gridcopy=document.getElementById("gridcopy");

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
function hideView(){
  $("#hidecarry").css('display','none');
  $(".mobile-navi").fadeOut(50);
  $("#showcarry").show();
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
var sheet_name;
function getSheetname(s){
    sheet_name=s;
}
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

var cdg = canvasDatagrid({
  parentNode: _grid
});
cdg.style.height = '100%';
cdg.style.width = '100%';

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
var headerMap = new Map();

var _onsheet = function(json, sheetnames, select_sheet_cb) {

  document.getElementById('footnote').style.display = "none";
  click_ctr++;

  make_buttons(sheetnames, select_sheet_cb);

  /* show grid */
  _grid.style.display = "inline-block";
  _resize();


  if (sheetName === undefined){ // sheetname will be undefined for the first sheet so set it to the default
     sheetName = sheetnames[0];
  }

  /* clean json */
  var L = 0;
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
  json = cleanJson;
  var emptySheet;
  /* set up table headers */
  if(headerMap.has(sheetName)){
    cdg.data = json;
    console.log(headerMap.get(sheetName));
    changeHeader(headerMap.get(sheetName),1);
  }
  else{
    headerMap.set(sheetName, json[0]);

    if(json.length==1){
      cdg.data = json;
      emptySheet=0;
    }
    else{
      cdg.data = json.slice(1);
    }
    changeHeader(json[0],emptySheet);
  }

  cdg.draw();
};
if(document.getElementById("headerdetails").value==" "){
  document.getElementById('showcarry').setAttribute("disabled", "disabled");
}
cdg.addEventListener('contextmenu', function (e) {
    e.items.push({
        title: 'Choose Header or Description',
        items: [
            {
                title: 'Header',
                click: function (ev) {
                    var val;
                    val=cdg.schema[e.cell.columnIndex].title
                    var header_location=sheetName+"-"+val;
                    var header_=document.getElementById("headerdetails");
                    header_.value=header_location;
                    document.getElementById('showcarry').removeAttribute("disabled");

                    storeHeader(header_location);
                }
            },
            {
                title: 'Description',
                click: function (ev) {
                    var val;
                    val=cdg.schema[e.cell.columnIndex].title
                    var desc_location=sheetName+"-"+val;
                    var desc_=document.getElementById("descdetails");
                    desc_.value=desc_location;
                    storeDesc(desc_location);
                }
            }
        ]
    });
});

function storeHeader(header_location){

  $.ajax({
    type : 'GET',
    // url : 'http://localhost:9000/hadatac/dd_editor/getHeaderLoc',
    url : '/hadatac/dd_editor/getHeaderLoc',
    data : {
      header_loc: header_location

    },
    success : function(data) {

    },

  });

}

function storeDesc(desc_location){

  $.ajax({
    type : 'GET',
    // url : 'http://localhost:9000/hadatac/dd_editor/getCommentLoc',
    url : '/hadatac/dd_editor/getCommentLoc',
    data : {
      desc_loc: desc_location

    },
    success : function(data) {

    },

  });

}
// function saveLocations(){
//   location.reload();
//   var msg="Locations stored Successfully";
//   document.getElementById("alarmmsg").innerHTML = msg;

//   setTimeout(function(){
//       document.getElementById("alarmmsg").innerHTML = '';
//   }, 3000);
// }
// alert(document.getElementById("headerdetails").value);
// console.log(document.getElementById("headerdetails"));
