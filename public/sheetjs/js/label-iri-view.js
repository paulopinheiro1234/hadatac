




function fromSuggestionstoLabel(val,r,c){
 
   
  
  var labelStr = val.split("/").pop();
  
  var label1 = labelStr.split('_')[0];
  var label2 = labelStr.split('_')[1];
  
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

function showLabels(){

  hideView();
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

