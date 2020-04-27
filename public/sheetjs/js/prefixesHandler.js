
var prefixD = {};
var newPrefix=[]
function SDDPrefixtoJSON(){
    var oReq = new XMLHttpRequest();
    oReq.open("GET", sdd_url, true);
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
       
  
       var prefix = workbook.SheetNames[1];
       /* Get worksheet */
       var worksheet = workbook.Sheets[prefix];
       
       var xlarray=XLSX.utils.sheet_to_json(worksheet, {
           raw: true
       });
       
       if(xlarray.length==0){
            //prefixD[ontsList[i].URI]=labelsList[i].Prefix
            for(var i=0;i<ontsList.length;i++){
                //row returns to the original state
                
                XLSX.utils.sheet_add_aoa(worksheet, [[labelsList[i],ontsList[i]]], {origin: -1});
               
              }
            
            var xlarray=XLSX.utils.sheet_to_json(worksheet, {
                raw: true
            });
            //console.log(xlarray)
            
            for(var i=0;i<xlarray.length;i++){
                var temp=[];
                prefixD[xlarray[i].URI]=xlarray[i].Prefix;
                temp.push(xlarray[i].Prefix);
                temp.push(xlarray[i].URI);
                newPrefix.push(temp)
            }
            //console.log(labelsList);
            //console.log(ontsList);
       }
       else if(xlarray.length>1){
        for(var i=0;i<xlarray.length;i++){
                prefixD[xlarray[i].URI]=xlarray[i].Prefix;
        }
       }
      
   }
  
   oReq.send();
  
  
  }