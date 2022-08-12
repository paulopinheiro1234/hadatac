var currentTab = 0; // Current tab is set to be the first tab (0)
var formOn = false;

const ddForm = document.getElementsByClassName('setDDForm')[0];

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
   console.log(path);
   console.log(path['path']);
   var directoryData = null

   $.ajax({
      type : 'POST',
      url : '/hadatac/sddeditor_v2/ddFiles',
      async: false,
      data: { path: path['path'] } ,

      success : function(data) {
         console.log('vvvvvvvvvvv');
         console.log(data);
         directoryData = [];

         // Iterate over files
         for (var i = 0; i < data.files.length; i++) {
            var filename = data.files[i];
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
                     hasSubfolder: false
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


         // Old
         // for (var i = 0; i < data.children.length; i++) {
         //    var nodeName = data.children[i].name;
         //    console.log(nodeName);
         //
         //    var name = nodeName.split('/').slice(-1)[0];
         //    console.log(name);
         //
         //    if(nodeName.startsWith('+')){ // is a folder
         //       // Check for subfolders
         //       var hasSubfolder = false;
         //       for (var j = 0; j < data.children[i].children.length; j++) {
         //          var innerNodeName = data.children[i].children[j].name;
         //          if(innerNodeName.startsWith('+')){
         //             hasSubfolder = true;
         //             break;
         //          }
         //       }
         //
         //       // Add folder data
         //       directoryData.push({
         //          label: name,
         //          path: mergePath(path['path'], name),
         //          isFolder: true,
         //          isDrive: false,
         //          hasSubfolder: hasSubfolder
         //       });
         //
         //    }
         //    else {
         //       // Get file and ext name
         //       var fileNameParts = nodeName.split('.');
         //
         //       // filter out illegal dd files
         //       if(fileNameParts.length > 1){ // < 1 are hidden files
         //          var extName = fileNameParts[fileNameParts.length - 1];
         //          if (extName == 'xlsx') {
         //             directoryData.push({
         //                label: name,
         //                path: mergePath(path['path'], name),
         //                ext: extName,
         //                isFolder: false,
         //                isDrive: false,
         //                hasSubfolder: false
         //             });
         //          }
         //       }
         //    }
         // }



      }
   });

   console.log(directoryData);
   console.log('vvvvvvvvvvv');

   return directoryData;
}

$("#fileexplorer").jQueryFileExplorer({
   root: "/",
   rootLabel: "HADatAc",
   script: getDataDictionary
});

//  Setup button listener
const btn = document.getElementById('set_DD');
btn.addEventListener('click', () => {
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
});

// Set starting tab and ghide by default
showTab(currentTab); // Display the current tab
ddForm.style.display = 'none';

function showTab(n) {
   // This function will display the specified tab of the form ...
   var x = document.getElementsByClassName("tab");
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
   // ... and run a function that displays the correct step indicator:
   fixStepIndicator(n)
}

function nextPrev(n) {
   // This function will figure out which tab to display
   var x = document.getElementsByClassName("tab");
   // Exit the function if any field in the current tab is invalid:
   if (n == 1 && !validateForm()) return false;
   // Hide the current tab:
   x[currentTab].style.display = "none";
   // Increase or decrease the current tab by 1:
   currentTab = currentTab + n;
   // if you have reached the end of the form... :
   if (currentTab >= x.length) {
      //...the form gets submitted:
      document.getElementById("regForm").submit();
      return false;
   }
   // Otherwise, display the correct tab:
   showTab(currentTab);
}

function validateForm() {
   // This function deals with validation of the form fields
   var x, y, i, valid = true;
   x = document.getElementsByClassName("tab");
   y = x[currentTab].getElementsByTagName("input");
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
         y[i].className += "ddInput";
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
