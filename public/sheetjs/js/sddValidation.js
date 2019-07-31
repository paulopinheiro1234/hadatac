function openSDD(sdd_url){ 
    console.log(sdd_url);
    
        
        // Generate data dictionary map be reading DD in
       
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
            
            /* Get worksheet */
            var worksheet = workbook.Sheets[workbook.SheetNames[3]];
            
            var xlarray=XLSX.utils.sheet_to_json(worksheet, {
                raw: true
            });
            var cellElements=[]
            console.log(xlarray);
            // for(var i=0;i<xlarray.length;i++){
            //     if(xlarray[i]!="Attribute"||xlarray[i]!="attributeOf"||xlarray[i]!="Unit"||
            //     xlarray[i]!="Time"||xlarray[i]!="Entity"||xlarray[i]!="Role"||xlarray[i]!="Relation"||
            //     xlarray[i]!="inRelationTo"||xlarray[i]!="wasDerivedFrom"||xlarray[i]!="wasGeneratedBy"){
            //         cellElements.push(xlarray[i])
            //     }
            // }
            
            for(var i=0;i<xlarray.length;i++){
                if(xlarray[i]["Attribute"]!=null){
                    cellElements.push(xlarray[i]["Attribute"]);
                }
                
                else if(xlarray[i]["Unit"]!=null){
                    cellElements.push(xlarray[i]["Unit"]);
                }
                
                else if(xlarray[i]["Entity"]!=null){
                    cellElements.push(xlarray[i]["Entity"]);
                }
                
                else if(xlarray[i]["Relation"]!=null){
                    cellElements.push(xlarray[i]["Relation"]);
                }
                
                // else if(xlarray[i]["wasDerivedFrom"]!=null){
                //     cellElements.push(xlarray[i]["wasDerivedFrom"]);
                // }
                // else if(xlarray[i]["wasGeneratedBy"]!=null){
                //     cellElements.push(xlarray[i]["wasGeneratedBy"]);
                //  }
            }
            console.log(cellElements);
            validateElements(cellElements);
            console.log(cellElements);
    
        }
        oReq.send();
}
function validateElements(cellElements){
    cellElements.push("sio:isPartOf");
    for(var i=0;i<cellElements.length;i++){
        
       var label= cellElements[i];
       var origlabel=label;
       label = label.split(":").pop();
        checkBlazegraph(label,origlabel);
    //    checkBlazegraph(cellElements[1])
       
    }
}
function checkBlazegraph(label,origlabel){

    var urlStr="<http://semanticscience.org/resource/"+label+">";
    console.log(urlStr);
    var url = "http://localhost:8080/blazegraph/namespace/store/sparql";
        
        var query=[
            "ASK{ ",urlStr,
            "?p",
            "?o ",
        "}"
        ].join(" ");
    var queryUrl = url+"?query="+ encodeURIComponent(query) +"&format=json";
    $.ajax({
    dataType: "json",  
    url: queryUrl,
    success: function( _data ) {
        console.log("reached");
        console.log(queryUrl);
        console.log(_data.boolean);
        if(_data.boolean==true){
            document.getElementById("found").style.color = 'green';
            document.getElementById('found').innerHTML += origlabel+": "+"found"+"<br />";
        }
        else if(_data.boolean==false){
            document.getElementById("notfound").style.color = 'red';
            document.getElementById('notfound').innerHTML += origlabel+": "+"not found"+"<br />";
        }
    }
    });
}

