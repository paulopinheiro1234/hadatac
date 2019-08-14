function verifySDD(){
    document.getElementById("irifound").innerHTML = "";
    document.getElementById("irinotfound").innerHTML = "";
    for (var i=1;i<copyOfR;i++){
      for(var j=1;j<copyOfL;j++){
          if(cdg.data[i][j]==null ||cdg.data[i][j].startsWith("??")){

          }
          else{
            
            var cellItem=cdg.data[i][j];
            
            var label= cellItem;
            var label1 = label.split(":")[0];
            var label2 = label.split(":")[1];
            getOwlUrl(label1,label2,label);
          }
        
        
        }
      }
    }
function getOwlUrl(label1,label2,label){
    if(label1=="sio"){
        var urlStr="<http://semanticscience.org/resource/"+label2+">";
    }
    else if(label1=="hasco"){
        var urlStr="<http://hadatac.org/ont/hasco/"+label2+">";
    }
    else if(label1=="chear"){
        var urlStr="<http://hadatac.org/ont/chear#"+label2+">";
    }
    else if(label1=="uberon"){
        var urlStr="<http://purl.obolibrary.org/obo/UBERON_"+label2+">";
    }
    searchForIri(label1,label2,label,urlStr);

}
function searchForIri(label1,label2,label,urlStr){
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
        
        if(_data.boolean==true){
            document.getElementById("irifound").style.color = 'green';
            document.getElementById('irifound').innerHTML += label+": "+"found"+"<br />";
        }
        else if(_data.boolean==false){
            document.getElementById("irinotfound").style.color = 'red';
            document.getElementById('irinotfound').innerHTML += label+": "+"not found"+"<br />";
        }
    }
    });
}


