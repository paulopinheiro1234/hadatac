var dd_data = null;


function decodeDDLink(ddLink){
   var ddarray = ddLink.split('?');
   ddarray.shift(); // Throw away the empty var
   if (ddarray.length != 4) {
      console.log("Bad data dictionary link, \"?\"s can't be in sheet or header names");
      return null;
   }

   // Grab the correct data
   var dd_data = {};
   for(var varText of ddarray){
      var varData = varText.split('=');
      if (varData.length != 2) {
         console.log("Bad data dictionary link, \"=\"s can't be in sheet or header names");
         return null;
      }
      dd_data[varData[0]] = varData[1];
   }

   // Maker sure that all the vars are valid
   for( var varcheck of ["fileID", "sheet", "header", "desc"]){
      if(!dd_data.hasOwnProperty(varcheck)){
         console.log("Bad data dictionary link, missing link variable " + varcheck);
         return null;
      }
   }

   return dd_data;
}

function stringToArrayBuffer(str) {
    var buf = new ArrayBuffer(str.length);
    var bufView = new Uint8Array(buf);

    for (var i=0, strLen=str.length; i<strLen; i++) {
        bufView[i] = str.charCodeAt(i);
    }

    return buf;
}

// This function converts string to binary, allowing us to make synch file requests
// https://stackoverflow.com/questions/44974003/recover-arraybuffer-from-xhr-responsetext
let iso_8859_15_table = { 338: 188, 339: 189, 352: 166, 353: 168, 376: 190, 381: 180, 382: 184, 8364: 164 }
function iso_8859_15_to_uint8array(iso_8859_15_str) {
    let buf = new ArrayBuffer(iso_8859_15_str.length);
    let bufView = new Uint8Array(buf);
    for (let i = 0, strLen = iso_8859_15_str.length; i < strLen; i++) {
        let octet = iso_8859_15_str.charCodeAt(i);
        if (iso_8859_15_table.hasOwnProperty(octet))
            octet = iso_8859_15_table[octet]
        bufView[i] = octet;
        if(octet < 0 || 255 < octet)
            console.error(`invalid data error`)
    }
    return bufView
}

function initDataDictionary(){
   // Get the latest data dictionary link
   var aoaData = null; // Get the latest data
   if(sheetName == "InfoSheet"){ // we are on the page so grab grid data
      aoaData = cdg.data
   }
   else{ // We are on another page so grab the data from the worksheet
      aoaData = XLSX.utils.sheet_to_json(workbook.Sheets["InfoSheet"], {header: 1});
   }

   if(aoaData == null){
      console.log("Bad SDD, missing the InfoSheet sheet. Can't set the data dictionary!");
      return false;
   }

   // Find the Data Dictionary Link row
   var ddLink = null;
   for(row in aoaData){
      if(aoaData[row][0] == "Data Dictionary Link"){
         if(aoaData[row].length > 1){
            ddLink = aoaData[row][1];
         }
         break; // Why keep looking?
      }
   }

   // Decode the dd link
   if(ddLink === null){
      console.log("Bad SDD, missing the Data Dictionary Link entry. Can't set the data dictionary!");
      return false;
   }
   else{
      var ddworkbook = decodeDDLink(ddLink);
      if(ddworkbook === null){
         console.log("Couldn't load Data Dictionary");
         return false;
      }

      // Download the data dictionary
      var oReq = new XMLHttpRequest();
      oReq.open("POST", "/hadatac/annotator/downloadfile?file_id=" + ddworkbook.fileID, false);
      // This call needs to by synch so that we can ensure data at the proper time
      // Todo this we need to ask for text instead of a binary becasue XMLHttpRequest
      // doesn't support synch binary calls.
      oReq.overrideMimeType('text/plain; charset=ISO-8859-15');
      oReq.onload = function(e) {
         /* convert data to binary string */
         var data = iso_8859_15_to_uint8array(oReq.response);
         var arr = new Array();
         for (var i = 0; i != data.length; ++i) arr[i] = String.fromCharCode(data[i]);
         var bstr = arr.join("");

         /* Call XLSX */
         ddworkbook["workbook"] = XLSX.read(bstr, {
            type: "binary"
         });

         var first_sheet_name = ddworkbook["workbook"].SheetNames[2];

         /* Get worksheet */
         var worksheet= XLSX.utils.sheet_to_json(ddworkbook["workbook"].Sheets[ddworkbook["sheet"]], {
            raw: true
         });

         dd_data = ddworkbook;
         dd_data['descMap'] = {};
         for (var row of worksheet) {
            dd_data['descMap'][row[ddworkbook["header"]]] = row[ddworkbook["desc"]];
         }
      }
      oReq.send();
   }

   return true;
}

// Takes in dd_data
function importDDrows(data){
   // Set data dictionary links
   var sddData = null; // Get the latest data
   if(sheetName == "Dictionary Mapping"){ // we are on the page so grab grid data
      sddData = cdg.data
   }
   else{ // We are on another page so grab the data from the worksheet
      sddData = XLSX.utils.sheet_to_json(workbook.Sheets["Dictionary Mapping"], {header: 1});
   }

   var sddVars = []
   var schemaLength = 0;
   for ( var row of sddData ){
      if(schemaLength == 0){
         schemaLength = row.length
      }
      else{
         sddVars.push(row[0])
      }
   }

   if ( schemaLength == 0){
      console.log("Error couldn't parse SDD during DD import!");
      return false;
   }

   // Create a new row for every missing column
   var newRowCount = 0;
   for ( var datum in data['descMap']){

      // Check if we have this variable
      if (!sddVars.includes(datum)){
         var row = [];
         for ( var i = 0; i < schemaLength; i++ ){
            row.push('');
         }
         row[0] = datum;
         sddData.push(row);
         newRowCount = newRowCount + 1;
      }
   }

   // Update other data structures to the new mapping page size
   createCopySheet(sddData);

   // We need to add new blank rows in the approval data structure
   for(var j = 0; j < newRowCount; j++){
      var appRow = [];
      for ( var i = 0; i < schemaLength; i++ ){
         appRow.push('');
      }
      approvalList.push(appRow);
   }

   workbook.Sheets["Dictionary Mapping"] = XLSX.utils.aoa_to_sheet(sddData);
   cdg.draw();

   return true;
}

function setDataDictionary(fileID, sheet, header, descrip, importMissing){
   const linkData = "?fileID=" + fileID + "?sheet=" + sheet + "?header=" + header + "?desc=" + descrip;

   // Set data dictionary links
   var aoaData = null; // Get the latest data
   if(sheetName == "InfoSheet"){ // we are on the page so grab grid data
      aoaData = cdg.data
   }
   else{ // We are on another page so grab the data from the worksheet
      aoaData = XLSX.utils.sheet_to_json(workbook.Sheets["InfoSheet"], {header: 1});
   }

   if(aoaData == null){
      console.log("Bad SDD, missing the InfoSheet sheet. Can't set the data dictionary!");
      return false;
   }

   // Find the Data Dictionary Link row
   var foundIt = false;
   for(row in aoaData){
      if(aoaData[row][0] == "Data Dictionary Link"){
         if(aoaData[row].length > 1){
            aoaData[row][1] = linkData;
         }
         else{
            aoaData[row].push(linkData);
         }
         foundIt = true;
         break; // Why keep looking?
      }
   }

   // We didn't find it so create it
   if(!foundIt){
      aoaData.push(["Data Dictionary Link", linkData])
   }

   // Save the changes to the workbook
   workbook.Sheets["InfoSheet"] = XLSX.utils.aoa_to_sheet(aoaData);
   cdg.draw();


   // Set the Data Dictionary in "memory"
   if(initDataDictionary()){
      // Import the Headers if checked
      // Check to see if we need to load in the rows
      if(importMissing){
         importDDrows(dd_data);
      }

      // Update the DD url and generate suggestions
      // getURL(url); // shouldn't need to pass the url anymore
      getSuggestion();

      return true;
   }
   else{
      console.log("Couldn't initalize data dictionary!");
      return false;
   }
}
