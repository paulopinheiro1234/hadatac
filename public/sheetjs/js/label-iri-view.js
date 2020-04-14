




function fromSuggestionstoLabel(val,r,c){
 
   
  

  $.ajax({
    type : 'GET',
    url : 'http://localhost:9000/hadatac/sddeditor_v2/getLabelFromIri',
    data : {
      iricode:val
    },
    success : function(data) {
     
      var newvalue=data;
      
      sheetStorage[r][c]=newvalue;
      
      cdgcopy.data = sheetStorage;
    }
  });
  

}
function convertOriginal(labelsCopy){
  
  for(var i=0;i<sheetStorage.length;i++){
    for(var j=0;j<sheetStorage[i].length;j++){
      if(sheetStorage[i][j]=="" || sheetStorage[i][j].startsWith("??")){

      }
      else if(sheetStorage[i][j].startsWith("http")){
      var labSheet=fromSuggestionstoLabel(sheetStorage[i][j],i,j,labelsCopy);
      }
      
    }
  }
  
  return labSheet
}
function showLabels(){
  
  hideView();
  emptySheet=[];
  labelsCopy=[];
  for(var i=0;i<sheetStorage.length;i++){
    emptySheet.push([])
    for(var j=0;j<sheetStorage[i].length;j++){
      emptySheet[i].push("")
      
    }
  }
  
  
  cdgcopy.data=emptySheet;
  
  _grid.style.display="none";
  _buttons.style.display="none";
  _footnote.style.display="none";
  _gridcopy.style.display="block";
  _gridcopy.style.height = (window.innerHeight - 300) + "px";
  _gridcopy.style.width = '100%';

  cdgcopy.data = sheetStorage;
  
  
  

}

function backToOriginal(){
  _gridcopy.style.display="none";
  _grid.style.display="block";
  _buttons.style.display="block";
  _footnote.style.display="block";
  
}

