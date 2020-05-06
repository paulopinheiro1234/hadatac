/* oss.sheetjs.com (C) 2014-present SheetJS -- http://sheetjs.com */
/* vim: set ts=2: */

var DropSheet = function DropSheet(opts) {
  if(!opts) opts = {};
  var nullfunc = function(){};
  if(!opts.errors) opts.errors = {};
  if(!opts.errors.badfile) opts.errors.badfile = nullfunc;
  if(!opts.errors.pending) opts.errors.pending = nullfunc;
  if(!opts.errors.failed) opts.errors.failed = nullfunc;
  if(!opts.errors.large) opts.errors.large = nullfunc;
  if(!opts.on) opts.on = {};
  if(!opts.on.workstart) opts.on.workstart = nullfunc;
  if(!opts.on.workend) opts.on.workend = nullfunc;
  if(!opts.on.sheet) opts.on.sheet = nullfunc;
  if(!opts.on.wb) opts.on.wb = nullfunc;

  var rABS = typeof FileReader !== 'undefined' && FileReader.prototype && FileReader.prototype.readAsBinaryString;
  var useworker = typeof Worker !== 'undefined';
  var pending = false;
  function fixdata(data) {
    var o = "", l = 0, w = 10240;
    for(; l<data.byteLength/w; ++l)
      o+=String.fromCharCode.apply(null,new Uint8Array(data.slice(l*w,l*w+w)));
    o+=String.fromCharCode.apply(null, new Uint8Array(data.slice(o.length)));
    return o;
  }

  function sheetjsw(data, cb, readtype) {
    pending = true;
    opts.on.workstart();
    var scripts = document.getElementsByTagName('script');
    var dropsheetPath;
    for (var i = 0; i < scripts.length; i++) {
      if (scripts[i].src.indexOf('dropsheet') != -1) {
        dropsheetPath = scripts[i].src.split('dropsheet.js')[0];
      }
    }
    var worker = new Worker(dropsheetPath + 'sheetjsw.js');
    worker.onmessage = function(e) {
      switch(e.data.t) {
        case 'ready': break;
        case 'e': pending = false; console.error(e.data.d); break;
        case 'xlsx':
          pending = false;
          opts.on.workend();
          cb(JSON.parse(e.data.d), 0); break;
      }
    };
    worker.postMessage({d:data,b:readtype,t:'xlsx'});
  }

  var last_wb;
  var last_sheetidx;

  function to_json(workbook) {
    if(useworker && workbook.SSF) XLSX.SSF.load_table(workbook.SSF);
    var result = {};
    workbook.SheetNames.forEach(function(sheetName) {
      var roa = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName], {header:1});
      if(roa.length > 0) result[sheetName] = roa;
    });
    return result;
  }

  function choose_sheet(sheetidx) {
	  if (typeof last_sheetidx !== 'undefined') {
		  last_wb.Sheets[last_wb.SheetNames[last_sheetidx]] = XLSX.utils.aoa_to_sheet(cdg.data);
	  }
	  process_wb(last_wb, sheetidx);
  }

  function process_wb(wb, sheetidx) {
    last_wb = wb;
    opts.on.wb(wb, sheetidx);
    var sheet = wb.SheetNames[sheetidx||0];
    var json = to_json(wb)[sheet];
    opts.on.sheet(json, wb.SheetNames, choose_sheet);
    last_sheetidx = sheetidx;
  }

  function handleDrop(e) {
    e.stopPropagation();
    e.preventDefault();
    if(pending) return opts.errors.pending();
    var files = e.dataTransfer.files;
    var i,f;
    for (i = 0, f = files[i]; i != files.length; ++i) {
      var reader = new FileReader();
      var name = f.name;
      reader.onload = function(e) {
        var data = e.target.result;
        var wb, arr;
        var readtype = {type: rABS ? 'binary' : 'base64' };
        if(!rABS) {
          arr = fixdata(data);
          data = btoa(arr);
        }
        function doit() {
          try {
            if(useworker) { sheetjsw(data, process_wb, readtype); return; }
            wb = XLSX.read(data, readtype);
            process_wb(wb, 0);
          } catch(e) { console.log(e); opts.errors.failed(e); }
        }

        if(e.target.result.length > 1e6) opts.errors.large(e.target.result.length, function(e) { if(e) doit(); });
        else { doit(); }
      };
      if(rABS) reader.readAsBinaryString(f);
      else reader.readAsArrayBuffer(f);
    }
  }

  function handleDragover(e) {
    e.stopPropagation();
    e.preventDefault();
    e.dataTransfer.dropEffect = 'copy';
  }

  if(opts.drop) {
    opts.drop.addEventListener('dragenter', handleDragover, false);
    opts.drop.addEventListener('dragover', handleDragover, false);
    opts.drop.addEventListener('drop', handleDrop, false);
  }

  function handleFile(e) {
    if(pending) return opts.errors.pending();
    var files = e.target.files;
    var i,f;
    for (i = 0, f = files[i]; i != files.length; ++i) {
      var reader = new FileReader();
      var name = f.name;
      reader.onload = function(e) {
        var data = e.target.result;
        var wb, arr;
        var readtype = {type: rABS ? 'binary' : 'base64' };
        if(!rABS) {
          arr = fixdata(data);
          data = btoa(arr);
        }
        function doit() {
          try {
            if(useworker) { sheetjsw(data, process_wb, readtype); return; }
            wb = XLSX.read(data, readtype);
            process_wb(wb, 0);
          } catch(e) { console.log(e); opts.errors.failed(e); }
        }

        if(e.target.result.length > 1e6) opts.errors.large(e.target.result.length, function(e) { if(e) doit(); });
        else { doit(); }
      };
      if(rABS) reader.readAsBinaryString(f);
      else reader.readAsArrayBuffer(f);
    }
  }
  
  function handleFileData(data) {
    if(pending) return opts.errors.pending();
    var wb, arr;
    var readtype = {type:"array"};
    function doit() {
      try {
        if(useworker) { sheetjsw(data, process_wb, readtype); return; }
        wb = XLSX.read(data, readtype);
        process_wb(wb, 0);
      } catch(e) { console.log(e); opts.errors.failed(e); }
    }

    if(data.length > 1e6) opts.errors.large(data.length, function(e) { if(e) doit(); });
    else { doit(); }
  }
  
  function handleFileUpload(e) {
	  if (opts.on.upload) {
		  opts.on.upload();
	  }
	  
	  var wopts = { bookType: opts.filetype, bookSST: false, type: 'array',compression:true};
	  if (typeof last_sheetidx !== 'undefined') {
		  last_wb.Sheets[last_wb.SheetNames[last_sheetidx]] = XLSX.utils.aoa_to_sheet(cdg.data);
	  }
	  
	  var wbout = XLSX.write(last_wb, wopts);
	  
	  var formdata = new FormData();
	  if (opts.formdata) {
		  formdata.append('file', new File([wbout], opts.formdata));
	  } else {
		  formdata.append('file', new File([wbout], 'sheetjs.' + filetype));
	  }
	
	  var xhr = new XMLHttpRequest();
	  xhr.open("POST", opts.upload_url, true);
	  xhr.onreadystatechange = opts.reponse_action;

	  xhr.send(formdata);
  }
  
  function handleFileDownload(e) {
	  var fileName = opts.on.download();
	  var wopts = { bookType:'xlsx', bookSST:false, type:'array' };
	  if (typeof last_sheetidx !== 'undefined') {
		  last_wb.Sheets[last_wb.SheetNames[last_sheetidx]] = XLSX.utils.aoa_to_sheet(cdg.data);
	  }
	  
	  XLSX.writeFile(last_wb, fileName);
  }

  if(opts.file && opts.file.addEventListener) opts.file.addEventListener('change', handleFile, false);
  
  if(opts.upload) opts.upload.addEventListener('click', handleFileUpload, false);
  
  if(opts.download) opts.download.addEventListener('click', handleFileDownload, false);
  
  if(opts.data) handleFileData(opts.data);
};
