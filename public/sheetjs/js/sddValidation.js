function verifySDD(){

    document.getElementById("recommendedterms").style.display="none";
    document.getElementById("recommendedcolumn").style.display="none";
    document.getElementById("editRowsAdd").style.display="none";
    document.getElementById("verifysdd").style.display="flex";
    document.getElementById("verifysdd").style.justifyContent="center";
    document.getElementById("verifysdd").style.margin="0 auto";
    document.getElementById("menulist").style.display="none";
    document.getElementById("virtuallist").style.display="none";
    document.getElementById("returnView").style.display="block";
    document.getElementById("searchForTerm").style.display="none";
    document.getElementById("numToSearch").style.display="none";
    document.getElementById("numberResults").style.display="none";
  
  


    if(cdg.data.length!=copyOfR){
        
        cdg.deleteRow(copyOfR);
    }
   
     document.getElementById("irifound").innerHTML = "";
    // document.getElementById("irinotfound").innerHTML = "";
    for (var i=1;i<copyOfR;i++){
      for(var j=1;j<copyOfL;j++){
          if(cdg.data[i][j]==null ||cdg.data[i][j].startsWith("??")||cdg.data[i][j]==''){
            
          }
          else{
            
            var cellItem=cdg.data[i][j];
            if(cellItem.endsWith(" * ")){
                
                cellItem=cellItem.replace(" * ",'');
                
            }
            
            
            var label= cellItem;
            
            var label1 = label.split(":")[0];
            var label2 = label.split(":")[1];
            getOwlUrl(label1,label2,label,i,j);
          }
        
        
        }
      }
    }
var notFound=[];
var numLabels=0
function getOwlUrl(label1,label2,label,i,j){
     console.log(label);
     numLabels+=1;
    if(label1=="sio"){
        var urlStr="<http://semanticscience.org/resource/"+label2+">";
    }
    else if(label1=="hasco"){
        var urlStr="<http://hadatac.org/ont/hasco/"+label2+">";
    }
    else if(label1=="hhear"){
        var urlStr="<http://hadatac.org/ont/hhear#"+label2+">";
    }
    else if(label1=="uberon"){
        var urlStr="<http://purl.obolibrary.org/obo/UBERON_"+label2+">";
    }
    searchForIri(label1,label2,label,urlStr,i,j);

}
var wrongterm;
var listOfTotalTerms=[];
var sheetdict=new Map();
function searchForIri(label1,label2,label,urlStr,i,j){
    var url = "http://localhost:8080/blazegraph/namespace/store/sparql";
        
        var query=[
            "ASK{",urlStr,
            "?p",
            "?o ",
        "}"
        ].join(" ");
    var queryUrl = url+"?query="+ encodeURIComponent(query) +"&format=json";
    $.ajax({
    
    dataType: "json",  
    url: queryUrl,
    success: function( _data ) {
        var noerr="No Errors Found!"+"<br />";
        
        
        var location=[];
        location.push(i);
        location.push(j);
        if(_data.boolean==true){

            location.push("found")
            if(!sheetdict.has(label2)){
                sheetdict.set(label2,location);
            }
            // else{

            // }
            
        }
        else if(_data.boolean==false){
            document.getElementById('irifound').innerHTML +=""
            document.getElementById('irifound').innerHTML +="Error(s) Found!"
            location.push("notfound")
            var rowErr=i+1;
            var colErr=j+1;
            var wrongInfo="Row "+rowErr+" ,Column "+j+":"+"("+label2+")"+" IRI not found! ";
            //document.getElementById('irifound').innerHTML +=wrongInfo;
            //cdg.gotoCell(i, j)
            // alert("hi!")
            
            var buttn = document.createElement("button");
            buttn.style.backgroundColor="lavender";
            buttn.style.color="black";
            buttn.style.fontWeight="bold";
            buttn.style.border="transparent";
            buttn.style.border="0";
            buttn.style.width="350px";
            buttn.style.fontFamily="Optima, sans-serif";
            buttn.style.fontsize="11pt";
            buttn.style.textAlign="center";
            buttn.innerHTML=label2;
            document.getElementById('irifound').appendChild(buttn);

            var buttn2 = document.createElement("button");
            buttn2.style.backgroundColor="aliceblue";
            buttn2.style.color="black";
            buttn2.style.border="transparent";
            buttn2.style.border="0";
            buttn2.style.width="350px";
            buttn2.style.fontFamily="Optima, sans-serif";
            buttn2.style.fontsize="11pt";
            buttn2.style.textAlign="center";
            buttn2.innerHTML="Row: "+rowErr+" Column: "+colErr;
            document.getElementById('irifound').appendChild(buttn2);


            if(!sheetdict.has(label2)){
                sheetdict.set(label2,location);
            }
            if (document.addEventListener) { // IE >= 9; other browsers
                buttn.addEventListener('click', function(e) {
                    wrongterm= sheetdict.get(this.innerHTML);
                    var errRow=location[0];
                    var errCol=location[1];
                    cdg.gotoCell(errRow, errCol)
                    //alert(wrongterm,errRow,errCol);
                  
               
                    
         
                }, false);
                
             } 
             
             

            
            
        }
    noErrDisplay(sheetdict,label2);
    }
});
}
var foundArray=[];
function noErrDisplay(sheetdict,label2){
    //console.log(sheetdict);
    
    //document.getElementById('irifound').innerHTML="";
 
    
    if(sheetdict.get(label2)[2]=="found"){
        foundArray.push("found");
    }
    
    if(foundArray.length==numLabels){
        
        var b1 = document.createElement("button");
        b1.style.backgroundColor="lavender";
        b1.style.fontWeight="bold";
        b1.style.color="green";
        b1.style.border="transparent";
        b1.style.border="0";
        b1.style.width="350px";
        b1.style.fontFamily="Optima, sans-serif";
        b1.style.fontsize="14pt";
        b1.style.textAlign="center";
        b1.innerHTML="No Errors Found!"+"<br />";
        document.getElementById('irifound').appendChild(b1);
        
    }
    //console.log(sheetdict.get(label2)[2])
}

function externalValidate(f_id){
    
   
    document.getElementById("irifound").style.display="inline-block";
    document.getElementById('irifound').innerHTML="";
    if(document.getElementById('irifound').innerHTML==""){
        document.getElementById('loadingmsg').innerHTML="Verifying . . .";
        var loader = document.createElement("img");
        loader.setAttribute("src", "https://thumbs.gfycat.com/SkinnySeveralAsianlion-max-1mb.gif");
        loader.setAttribute("height", "30");
        loader.setAttribute("width", "30");
        document.getElementById('loadingmsg').appendChild(loader);
    }
    else if(document.getElementById('irifound').innerHTML!=""){
        document.getElementById('loadingmsg').innerHTML="";
        
    }
    document.getElementById("recommendedterms").style.display="none";
    document.getElementById("recommendedcolumn").style.display="none";
    document.getElementById("editRowsAdd").style.display="none";
    document.getElementById("verifysdd").style.display="flex";
    document.getElementById("verifysdd").style.justifyContent="center";
    document.getElementById("verifysdd").style.margin="0 auto";
    document.getElementById("listingTable").style.display="none";
    document.getElementById("returnView").style.display="block";
    document.getElementById("searchForTerm").style.display="none";
    document.getElementById("numToSearch").style.display="none";
    document.getElementById("menulist").style.display="none";
    document.getElementById("virtuallist").style.display="none";
    document.getElementById("numberResults").style.display="none";
    var fid=f_id;
    
    $.ajax({
       type : 'GET',
       url : 'http://localhost:9000/hadatac/working/verifyDataFileTemp',
       data : {
         file_id:fid
       },
       success : function(data) {
          console.log(data);
          var annotationLogs = decodeURIComponent(data);
          annotationLogs=annotationLogs.replace(/&amp;lt;br&amp;gt;/g, '<br>').replace(/&amp;lt;/g, '&lt;').replace(/&amp;gt;/g, '&gt;');
          var arrayt= annotationLogs.split('&lt;br&gt;');
          
          
          console.log(arrayt);   
          
            
         
          for(var i=0;i<arrayt.length;i++){
            var timeType= arrayt[i].split('[WARNING]')[0];
            var errorTypeDescription= arrayt[i].split('[WARNING]')[1];
            var logType= arrayt[i].split('[LOG]')[0];
            var logErrorTypeDescription= arrayt[i].split('[LOG]')[1];
            if(arrayt[i].includes('[WARNING]')){
                var buttn = document.createElement("button");
                buttn.style.backgroundColor="Moccasin";
                buttn.style.color="black";
                buttn.style.fontWeight="bold";
                buttn.style.border="transparent";
                buttn.style.border="0";
                buttn.style.width="350px";
                buttn.style.fontFamily="Optima, sans-serif";
                buttn.style.fontsize="11pt";
                buttn.style.textAlign="center";
                buttn.innerHTML=timeType+" [WARNING]";
                document.getElementById('irifound').appendChild(buttn);

                var buttn2 = document.createElement("button");
                buttn2.style.backgroundColor="LemonChiffon";
                buttn2.style.color="black";
                buttn2.style.border="transparent";
                buttn2.style.border="0";
                buttn2.style.width="350px";
                buttn2.style.fontFamily="Optima, sans-serif";
                buttn2.style.fontsize="11pt";
                buttn2.style.textAlign="center";
                buttn2.innerHTML=errorTypeDescription;
                document.getElementById('irifound').appendChild(buttn2);
                document.getElementById('irifound').style.height="350px";
                if(document.getElementById('irifound').innerHTML!=""){
                    document.getElementById('loadingmsg').innerHTML="";
                }
                if (document.addEventListener) { // IE >= 9; other browsers
                    buttn.addEventListener('click', function(e) {
                        wrongterm= sheetdict.get(this.innerHTML);
                        var errRow=location[0];
                        var errCol=location[1];
                        cdg.gotoCell(errRow, errCol)
                        //alert(wrongterm,errRow,errCol);
                      
                   
                        
             
                    }, false);
                    
                 } 

            }
            else if(arrayt[i].includes('[LOG]')){
                var buttn = document.createElement("button");
                buttn.style.backgroundColor="lavender";
                buttn.style.color="black";
                buttn.style.fontWeight="bold";
                buttn.style.border="transparent";
                buttn.style.border="0";
                buttn.style.width="350px";
                buttn.style.fontFamily="Optima, sans-serif";
                buttn.style.fontsize="11pt";
                buttn.style.textAlign="center";
                buttn.innerHTML=logType+" [LOG]";
                document.getElementById('irifound').appendChild(buttn);

                var buttn2 = document.createElement("button");
                buttn2.style.backgroundColor="aliceblue";
                buttn2.style.color="black";
                buttn2.style.border="transparent";
                buttn2.style.border="0";
                buttn2.style.width="350px";
                buttn2.style.fontFamily="Optima, sans-serif";
                buttn2.style.fontsize="11pt";
                buttn2.style.textAlign="center";
                buttn2.innerHTML=logErrorTypeDescription;
                document.getElementById('irifound').appendChild(buttn2);
                if(document.getElementById('irifound').innerHTML!=""){
                    document.getElementById('loadingmsg').innerHTML="";
                }
            }
          }          

       }
    });
 }

 