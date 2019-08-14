
function checkRecs (L,R,checker){
    copyOfL=L;
    copyOfR=R;
    var cellName;
    var colIndex=0;
    var rowIndex=0;
    var isVirtual=0;
    
    for (var i=0;i<R;i++){
      for(var j=1;j<L;j++){
        //cdg.data[i][j]=cdg.data[i][j]+" * ";
        rowIndex=i;
        colIndex=j;
        var colval=cdg.schema[colIndex].title;
        colval=colval.charAt(0).toLowerCase() + colval.slice(1);
        var rowval=cdg.data[rowIndex][0];       
        
        if(checker==1){
          if(colval=="Attribute"||colval=="Role"||colval=="Unit"||colval=="attribute"){
            isVirtual=0;
          }
          else if(colval=="attributeOf"||colval=="Time"||colval=="inRelationTo"||colval=="wasDerivedFrom"||colval=="wasGeneratedBy"
          || colval=="Relation"||colval=="Entity"){
            isVirtual=1;
          }
          var menuoptns=[];
          starRec(colval,rowval,menuoptns,isVirtual,L,R,rowIndex,colIndex);
  
        }
        else{
          //str.replace('a', '');
          if(cdg.data[i][j]!=null){
            cdg.data[i+1][j]=cdg.data[i][j].replace(' * ','');
          }
        }
    
    }
    
  }

}

  function starRec(colval,rowval,menuoptns,isVirtual,L,R,rowIndex,colIndex){
    var getJSON = function(url, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = 'json';
    xhr.onload = function() {
        var status = xhr.status;
        if (status == 200) {
            callback(null, xhr.response);
        } else {
            callback(status);
        }
    };
  
    xhr.send();
    };
  
    getJSON('http://128.113.106.57:5000/get-sdd/',  function(err, data) {
    if (err != null) {
        console.error(err);
    }
    
    else {
      if(rowval.startsWith("??")){
        var keyword="virtual-columns";
        helperStarRec(keyword,rowval,colval,data,menuoptns,isVirtual,L,R,rowIndex,colIndex);
    }
      else{
        var keyword="columns";
        helperStarRec(keyword,rowval,colval,data,menuoptns,isVirtual,L,R,rowIndex,colIndex);
        }
    }
    });
  }

  function helperStarRec(keyword,rowval,colval,data,menuoptns,isVirtual,L,R,rowIndex,colIndex){
    var virtualarray=Object.keys(data["sdd"]["Dictionary Mapping"][keyword]);
      var index=0;
      var checkcolval="";
      for (var i =0;i<data["sdd"]["Dictionary Mapping"][keyword].length;i++){
        if(data["sdd"]["Dictionary Mapping"][keyword][i]["column"]==rowval){
          index=i;
        }
      }
      var tempcolarray=Object.keys(data["sdd"]["Dictionary Mapping"][keyword][index]);
      for (var m=0;m<tempcolarray.length;m++){
          if(tempcolarray[m]==colval){
            checkcolval=tempcolarray[m];
          }
        }
      
      if(data["sdd"]["Dictionary Mapping"][keyword][index]["column"]==rowval && colval==checkcolval){
          for(var n=0;n<data["sdd"]["Dictionary Mapping"][keyword][index][colval].length;n++){
            var temp=[];
            temp.push(data["sdd"]["Dictionary Mapping"][keyword][index][colval][n].star);
            temp.push(data["sdd"]["Dictionary Mapping"][keyword][index][colval][n].value);
            menuoptns.push(temp);
          }
      
      }    
     
     if(menuoptns.length>0){
         drawStars(rowIndex,colIndex);
     }
     
}

function drawStars(rowIndex,colIndex){
    if(cdg.data[rowIndex][colIndex].includes(" * ")){
        
    }
    else{
      cdg.data[rowIndex][colIndex]+=" * ";
      cdg.draw();
    }
    

    
}

function stripStars(){
  console.log(copyOfL,copyOfR);
  //str.replace('a', '');
  checkRecs(copyOfL,copyOfR,0);
}


$.ajax({
    type : 'GET',
    url : 'http://localhost:9000/hadatac/annotator/sddeditor_v2/getCart',
    data : {
      //  s: str
    },
    success : function(data) {
    
    
        
      var select=document.getElementById("seecart"),data;
      for(var i=0;i<data.length;i++){  
          var li = document.createElement("li");                
          li.appendChild(document.createTextNode(data[i]+" "));
          li.setAttribute("class","inCart");
          select.appendChild(li);
          li.addEventListener("click",function(e){
            
            var newOntology=e.target.innerHTML.split(",")[1];
            // //addFromCart(newOntology);
            //alert(newOntology);
            
            cdg.data[rowNum][colNum]=newOntology;
            var colNum_str=colNum.toString();
            var rowNum_str=rowNum.toString();
            storeThisEdit(rowNum_str,colNum_str,cdg.data[rowNum][colNum]);
            cdg.draw();
          })
                          
        }
      }
    
      
        
    
});
      
function storeThisEdit(rowNum_str,colNum_str,changeValue){
  $.ajax({
    type : 'GET',
    url : 'http://localhost:9000/hadatac/annotator/sddeditor_v2/addToEdits',
    data : {
      row: rowNum_str,
      col:colNum_str,
      editValue: changeValue
    },
    success : function(data) {
      
    }
  });
}

function undoEdit(){
  $.ajax({
    type : 'GET',
    url : 'http://localhost:9000/hadatac/annotator/sddeditor_v2/getEdit',
    data : {
       //editValue: changeValue
    },
    success : function(data) {
      var rnum=Number(data[0]);
      var cnum=Number(data[1]);
      var valueRevert=data[2];
      cdg.data[rnum][cnum]=valueRevert;
      cdg.draw();
    }
  });
}

function reundoEdit(){
  $.ajax({
    type : 'GET',
    url : 'http://localhost:9000/hadatac/annotator/sddeditor_v2/getOldEdits',
    data : {
       //editValue: changeValue
    },
    success : function(data) {
      var rnum=Number(data[0]);
      var cnum=Number(data[1]);
      var valueRevert=data[2];
      cdg.data[rnum][cnum]=valueRevert;
      cdg.draw();
      
    }
  });
}

function revertRow(){
  
  var temp=[];
  for(var i=1;i<storeRow[storeRow.length-1].length;i++){
    temp.push(storeRow[storeRow.length-1][i]);
  }
  cdg.insertRow(temp,storeRow[storeRow.length-1][0]);
  storeRow.pop();
  
}
