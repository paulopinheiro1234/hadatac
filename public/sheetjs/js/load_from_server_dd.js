/* oss.sheetjs.com (C) 2014-present SheetJS -- http://sheetjs.com */
/* vim: set ts=2: */

var demo_enabled = true;
var sdd_suggestions;

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

/* make the buttons for the sheets */
var sheet_name;
function getSheetname(s){
    sheet_name=s;
}
var make_buttons = function(sheetnames, cb) {
  var buttons = document.getElementById('buttons');
  buttons.innerHTML = "";
  sheetnames.forEach(function(s,idx) {
    var btn = document.createElement('button');
    btn.style.height='35px';
    btn.style.padding='5px 5px 5px 5px';
    btn.type = 'button';
    btn.name = 'btn' + idx;
    btn.text = s;
    var txt = document.createElement('h5');
    txt.innerText = s;
    btn.appendChild(txt);
    btn.addEventListener('click', function() {cb(idx);getSheetname(s);}, false);
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
  _grid.style.height = (window.innerHeight - 200) + "px";
  _grid.style.width = (window.innerWidth - 100) + "px";
}
_resize();

window.addEventListener('resize', _resize);
var click_ctr=0;
var _onsheet = function(json, sheetnames, select_sheet_cb) {

  document.getElementById('footnote').style.display = "none";
  click_ctr++;
  // console.log(click_ctr);
  make_buttons(sheetnames, select_sheet_cb);

  /* show grid */
  _grid.style.display = "block";
  _resize();

  /* set up table headers */
  var L = 0;
  var R=0;
  json.forEach(function(r) { if(L < r.length) L = r.length; });
  
  
  for(var i = json[0].length; i < L; ++i) {
    json[0][i] = "";
  }
  cdg.data = json;
  for(var i=0;i<cdg.data[0].length;i++){
    cdg.schema[i].title = cdg.data[0][i];   
  }
  cdg.draw();
};

cdg.addEventListener('contextmenu', function (e) {
    e.items.push({
        title: 'Choose Header or Description',
        items: [
            {
                title: 'Header',
                click: function (ev) {
                    var val;
                    val=cdg.schema[e.cell.columnIndex].title
                    var header_location=sheet_name+"-"+val;
                    var header_=document.getElementById("headerdetails");
                    header_.value=header_location;
                    storeHeader(header_location);
                }
            },
            {
                title: 'Description',
                click: function (ev) {
                    var val;
                    val=cdg.schema[e.cell.columnIndex].title
                    var desc_location=sheet_name+"-"+val;
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
    url : 'http://localhost:9000/hadatac/annotator/dd_editor/getHeaderLoc',
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
    url : 'http://localhost:9000/hadatac/annotator/dd_editor/getDescLoc',
    data : {
      desc_loc: desc_location
      
    },
    success : function(data) {
     
    },
  
  });
  
}
function saveLocations(){
  location.reload();
  var msg="Locations stored Successfully";
  document.getElementById("alarmmsg").innerHTML = msg;

  setTimeout(function(){
      document.getElementById("alarmmsg").innerHTML = '';
  }, 3000);
}
