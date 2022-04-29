



function convertToLabel(val){
  var ret;
  $.ajax({
    type : 'GET',
    // url : 'http://localhost:9000/hadatac/sddeditor_v2/getLabelFromIri',
    url : '/hadatac/sddeditor_v2/getLabelFromIri',
    data : {
      iricode:val
    },
    async: false,
    success : function(data) {
      ret=data;
      // var newvalue=data;

      // sheetStorage[r][c]=newvalue;

      // cdgcopy.data = sheetStorage;
    }
  });
  return ret;

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
  for(var i=0;i<cdgcopy.schema.length;i++){
    cdgcopy.schema[i].title=globalHeaders[i];
  }
  cdgcopy.draw();




}

function backToOriginal(){
  _gridcopy.style.display="none";
  _grid.style.display="block";
  _buttons.style.display="block";
  _footnote.style.display="block";

}
