var currentTab = 0; // Current tab is set to be the first tab (0)
var formOn = false;


const ddForm = document.getElementsByClassName('setDDForm')[0];
const dd_path_field = document.getElementById('dd_path_field');
const sheet_explorer = document.getElementById('sheetexplorer');
const header_explorer = document.getElementById('headerexplorer');
const description_explorer = document.getElementById('descriptionexplorer');
const dd_import = document.getElementById('importDD');

var dd_path_updated = false;
var dd_sheet_updated = false;
var dd_header_updated = false;

var ddFileID = null;
var dd_workbook = null;
var dd_sheet = null;
var headers = null;
var dd_header = null;
var dd_descrip = null;
var importMissing = false;

function resetForm(){
   currentTab = 0;
   dd_path_updated = false;
   dd_sheet_updated = false;
   dd_header_updated = false;

   ddFileID = null;
   dd_workbook = null;
   dd_sheet = null;
   headers = null;
   dd_header = null;
   dd_descrip = null;
   importMissing = false;
}

function mergePath(path, filename){
   var combined = path;
   if(path.slice(-1) == '/'){
      combined = combined + filename;
   }
   else{
      combined = combined + '/' + filename;
   }
   return combined;
}

// Get Files
function getDataDictionary(path){
   var directoryData = null

   $.ajax({
      type : 'POST',
      url : '/hadatac/sddeditor_v2/ddFiles',
      async: false,
      data: { path: path['path'] } ,

      success : function(data) {
         directoryData = [];

         // Iterate over files
         for (var i = 0; i < data.files.length; i++) {
            var filename = data.files[i].name;
         // for (var filename in data['files']) {
            // Get file and ext name
            var fileNameParts = filename.split('.');

            // filter out illegal dd files
            if(fileNameParts.length > 1){ // < 1 are hidden files
               var extName = fileNameParts[fileNameParts.length - 1];
               if (extName == 'xlsx') {
                  directoryData.push({
                     label: filename,
                     path: mergePath(path['path'], filename),
                     ext: extName,
                     isFolder: false,
                     isDrive: false,
                     hasSubfolder: false,
                     id: data.files[i].loc
                  });
               }
            }
         }

         // Iterate over folders with no subfolders
         for (var i = 0; i < data.foldersWithNoSub.length; i++) {
            var name = data.foldersWithNoSub[i].slice(0, -1);
            directoryData.push({
               label: name,
               path: mergePath(path['path'], name),
               isFolder: true,
               isDrive: false,
               hasSubfolder: false
            });
         }

         // Iterate over folders with folders
         for (var i = 0; i < data.foldersWithSub.length; i++) {
            var name = data.foldersWithSub[i].slice(0, -1);
            directoryData.push({
               label: name,
               path: mergePath(path['path'], name),
               isFolder: true,
               isDrive: false,
               hasSubfolder: true
            });
         }
      }
   });
   return directoryData;
}

function fileSelected(file){
   dd_path_field.value = file.path;
   dd_path_updated = true;
   dd_sheet_updated = true;
   dd_header_updated = true;
   ddFileID = file.id;
}

$("#fileexplorer").jQueryFileExplorer({
   root: "/",
   rootLabel: "HADatAc",
   script: getDataDictionary,
   fileScript: fileSelected
});

function toggleForm(){
   const gridDisplay = document.getElementById('grid');
   const gridButtons = document.getElementById('buttons');

   if (formOn) { // The form is on so turn it off
      ddForm.style.display = 'none';
      gridDisplay.style.display = 'block';
      gridButtons.style.display = 'block';

   }
   else { // The form is off so turn it on
      ddForm.style.display = 'block';
      gridDisplay.style.display = 'none';
      gridButtons.style.display = 'none';
   }
   formOn = !formOn;
}

//  Setup button listener
const btn = document.getElementById('set_DD');
btn.addEventListener('click', () => {
   toggleForm();
});

function removeAllChildNodes(parent) {
    while (parent.firstChild) {
        parent.removeChild(parent.firstChild);
    }
}

// Set starting tab and ghide by default
showTab(currentTab); // Display the current tab
ddForm.style.display = 'none';

function showTab(n) {
   // This function will display the specified tab of the form ...
   var x = document.getElementsByClassName("ddtab");
   x[n].style.display = "block";
   // ... and fix the Previous/Next buttons:
   if (n == 0) {
      document.getElementById("prevBtn").style.display = "none";
   } else {
      document.getElementById("prevBtn").style.display = "inline";
   }
   if (n == (x.length - 1)) {
      document.getElementById("nextBtn").innerHTML = "Submit";
   } else {
      document.getElementById("nextBtn").innerHTML = "Next";
   }


   // Check if we need to update tab 2
   if( (n == 3) && dd_header_updated ){
      // Clean up old nodes
      removeAllChildNodes(description_explorer);

      // Create radio buttons
      for (var header in headers){
         if(header != dd_header){
            var radiobox = document.createElement('input');
            radiobox.type = 'radio';
            radiobox.id = header;
            radiobox.value = header;
            radiobox.name = 'header_select';
            radiobox.className = 'radiobox';

            var label = document.createElement('label')
            label.className = 'radiolabel';
            label.htmlFor = header;

            var example = " [";
            for(var e of headers[header]){
               example = example + "\"";
               example = example + e;
               example = example + "\", ";
            }
            example = example.slice(0, -2) + "]";

            var description = document.createTextNode(header + example);
            label.appendChild(description);

            var newline = document.createElement('br');

            description_explorer.appendChild(radiobox);
            description_explorer.appendChild(label);
            description_explorer.appendChild(newline);
         }
      }

      dd_header_updated = false;
   }

   // Check if we need to update tab 2
   if( (n == 2) && dd_sheet_updated ){
      // Clean up old nodes
      removeAllChildNodes(header_explorer);

      // Gather all headers and sample cells from the sheet
      headers = {}
      for(var row of dd_workbook[dd_sheet]){
         for (var header in row){
            if(headers[header] === undefined){
               headers[header] = [];
            }

            // We only need 3 examples
            if(headers[header].length < 3){
               headers[header].push(row[header]);
            }
         }
      }

      // Create radio buttons
      for (var header in headers){
         var radiobox = document.createElement('input');
         radiobox.type = 'radio';
         radiobox.id = header;
         radiobox.value = header;
         radiobox.name = 'header_select';
         radiobox.className = 'radiobox';

         var label = document.createElement('label')
         label.className = 'radiolabel';
         label.htmlFor = header;

         var example = " [";
         for(var e of headers[header]){
            example = example + "\"";
            example = example + e;
            example = example + "\", ";
         }
         example = example.slice(0, -2) + "]";

         var description = document.createTextNode(header + example);
         label.appendChild(description);

         var newline = document.createElement('br');

         header_explorer.appendChild(radiobox);
         header_explorer.appendChild(label);
         header_explorer.appendChild(newline);
      }
      dd_sheet_updated = false;
   }

   if( (n == 1) && dd_path_updated ){
      // Clean up old nodes
      removeAllChildNodes(sheet_explorer);

      // Put up loading messages
      var loadingMessage = document.createTextNode("Loading File...");
      sheet_explorer.appendChild(loadingMessage);

      // Download the DD workbook
      var oReq = new XMLHttpRequest();
      oReq.open("POST", '/hadatac/annotator/downloadfile?file_id=' + ddFileID, true);
      oReq.responseType = "arraybuffer";
      oReq.onload = function(e) {
         var arraybuffer = oReq.response;

         /* convert data to binary string */
         var data = new Uint8Array(arraybuffer);
         var arr = new Array();
         for (var i = 0; i != data.length; ++i) arr[i] = String.fromCharCode(data[i]);
         var bstr = arr.join("");

         /* Call XLSX */
         var rawDDworkbook = XLSX.read(bstr, {
            type: "binary"
         });

         // Clean up workbook
         dd_workbook = {};
         for (var sheetname of rawDDworkbook.SheetNames){
            dd_workbook[sheetname.toString()] = XLSX.utils.sheet_to_json(rawDDworkbook.Sheets[sheetname], {
               raw: true
            });
         }

         // Remove the loading message...
         removeAllChildNodes(sheet_explorer);
         document.getElementById("nextBtn").style.display = "none";

         // Load the sheetnames onto the page
         for (var sheet in  dd_workbook) {

            var radiobox = document.createElement('input');
            radiobox.type = 'radio';
            radiobox.id = sheet;
            radiobox.value = sheet;
            radiobox.name = 'sheet_select';
            radiobox.className = 'radiobox';

            var label = document.createElement('label')
            label.className = 'radiolabel';
            label.htmlFor = sheet;

            var schema = " [";
            if( dd_workbook[sheet].length > 0 ){
               for(var schemaLabel in dd_workbook[sheet][0]){
                  schema = schema + "\"";
                  if(schemaLabel != "__EMPTY"){
                     schema = schema + schemaLabel;
                  }
                  schema = schema + "\", ";
               }
               schema = schema.slice(0, -2) + "]";
            }
            else{
               schema = schema + "]";
            }

            var description = document.createTextNode(sheet + schema);
            label.appendChild(description);

            var newline = document.createElement('br');

            sheet_explorer.appendChild(radiobox);
            sheet_explorer.appendChild(label);
            sheet_explorer.appendChild(newline);
         }

         // Turn next button back on
         document.getElementById("nextBtn").style.display = "inline";
         document.getElementById("prevBtn").style.display = "inline";
      };
      oReq.onerror = function(error){
         console.log( "Couldn't load file..." );
      	console.log( error );

         removeAllChildNodes(sheet_explorer);

         // Put up loading messages
         var loadingMessage = document.createTextNode("Couldn't Load File!");
         sheet_explorer.appendChild(loadingMessage);
      };
      oReq.send();

      dd_path_updated = false;
   }

   // ... and run a function that displays the correct step indicator:
   fixStepIndicator(n)
}

function nextPrev(n) {
   // This function will figure out which tab to display
   var x = document.getElementsByClassName("ddtab");
   // Exit the function if any field in the current tab is invalid:
   if (n == 1 && !validateForm()) return false;
   // Hide the current tab:
   x[currentTab].style.display = "none";
   // Increase or decrease the current tab by 1:
   currentTab = currentTab + n;
   // if you have reached the end of the form... :
   if (currentTab >= x.length) {
      //...the form gets submitted:
      setDataDictionary(ddFileID, dd_sheet, dd_header, dd_descrip, importMissing);
      toggleForm();
      resetForm();
      return false;
   }
   // Otherwise, display the correct tab:
   showTab(currentTab);
}

function validateRadio(radios){
   var value = null;

   // Look if a radio is checked and get value
   for (var radio of radios) {
      if(radio.checked){
         value = radio.value;
         break;
      }
   }

   // Set the class if correct or incorrect
   var parentTab = radios[0].parentElement;
   var invalid = parentTab.getElementsByClassName("radiolabel_invalid");
   var valid = parentTab.getElementsByClassName("radiolabel");


   var labels = null;
   if (invalid.length > 0){
      // need to convert from a live collection to an array
      labels = Array.prototype.slice.call( invalid, 0 );
   }
   else{
      labels = Array.prototype.slice.call( valid, 0 );
   }

   for (var i = 0; i < labels.length; i++) {
      if(value == null){
         labels[i].className = "radiolabel_invalid";
      }
      else {
         labels[i].className = "radiolabel";
      }
   }

   return value;
}

function validateForm() {
   // This function deals with validation of the form fields
   var x, y, i, valid = true;
   x = document.getElementsByClassName("ddtab");
   y = x[currentTab].getElementsByTagName("input");

   if(y[0].type == "radio") {
      var value = validateRadio(y);
      valid = value != null;

      if(currentTab == 1){
         if(dd_sheet != value){
            dd_sheet = value;
            dd_sheet_updated = true;
            dd_header_updated = true;
         }
      }

      if(currentTab == 2){
         if(valid){
            dd_header = value;
            dd_header_updated = true;
         }
      }

      if(currentTab == 3){
         if(valid){
            dd_descrip = value;
            importMissing = dd_import.checked;
         }
      }

   }
   else{
      // A loop that checks every input field in the current tab:
      for (i = 0; i < y.length; i++) {
         // If a field is empty...
         if (y[i].value == "") {
            // add an "invalid" class to the field:
            y[i].className = "ddInput_invalid";
            // and set the current valid status to false:
            valid = false;
         }
         else {
            y[i].className = "ddInput";
         }
      }
   }

   // If the valid status is true, mark the step as finished and valid:
   if (valid) {
      document.getElementsByClassName("step")[currentTab].className += " finish";
   }
   return valid; // return the valid status
}

function fixStepIndicator(n) {
   // This function removes the "active" class of all steps...
   var i, x = document.getElementsByClassName("step");
   for (i = 0; i < x.length; i++) {
      x[i].className = x[i].className.replace(" active", "");
   }
   //... and adds the "active" class to the current step:
   x[n].className += " active";
}
