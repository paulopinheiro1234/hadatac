



// function conversion(sheetStorage){
// for(var i=1;i<sheetStorage.length;i++){
//   for(var j=1;j<sheetStorage[i].length;j++){
//     console.log(sheetStorage[i][j]);
//     if(sheetStorage[i][j].includes("http")==true){
//       var r=i;
//       var c=j;
//     var irilabel = sheetStorage[i][j].replace(' * ','');
//     var labelStr = irilabel.split("/").pop();
      
//     var label1 = labelStr.split('_')[0];
//     var label2 = labelStr.split('_')[1];
//     $.ajax({
//       type : 'GET',
//       url : 'http://localhost:9000/hadatac/sddeditor_v2/getLabelFromIri',
//       data : {
//         iricode:irilabel
//       },
//       success : function(data) {
//         console.log(data);
//         console.log(label1+":"+data);
//         var newvalue=label1+":"+data;
//         console.log(i,j);
//         sheetStorage[r][c]=newvalue;
//       }
//     });

//   }
// }
// }
// }
function fromSuggestionstoLabel(val,r,c){
 
   
  console.log(val);
  var labelStr = val.split("/").pop();
  
  var label1 = labelStr.split('_')[0];
  var label2 = labelStr.split('_')[1];
  console.log(label1);
  console.log(label2);
  $.ajax({
    type : 'GET',
    url : 'http://localhost:9000/hadatac/sddeditor_v2/getLabelFromIri',
    data : {
      iricode:val
    },
    success : function(data) {
      console
      console.log(data);
      var newvalue=label1+":"+data;
      
      sheetStorage[r][c]=newvalue;
      console.log(sheetStorage);
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
  //console.log(sheetStorage);
  // for(var i=1;i<sheetStorage.length;i++){
  //   for(var j=1;j<sheetStorage[i].length;j++){
      
  //     if(sheetStorage[i][j].includes("http")==true){
  //       var r=i;
  //     var c=j;
  //      console.log(sheetStorage[i][j]);
  //     if()
  //     var irilabel = sheetStorage[i][j].replace(' * ','');
  //     var labelStr = irilabel.split("/").pop();
        
  //     var label1 = labelStr.split('_')[0];
  //     var label2 = labelStr.split('_')[1];
  //     $.ajax({
  //       type : 'GET',
  //       url : 'http://localhost:9000/hadatac/sddeditor_v2/getLabelFromIri',
  //       data : {
  //         iricode:irilabel
  //       },
  //       success : function(data) {
          
  //         console.log(label1+":"+data);
  //         var newvalue=label1+":"+data;
          
  //         sheetStorage[r][c]=newvalue;
  //         console.log(sheetStorage);
  //         cdgcopy.data = sheetStorage;
  //       }
  //     });
  
  //   }
  // }
  // }
  // console.log(sheetStorage);
  cdgcopy.data = sheetStorage;
  
  

//     cdg.addEventListener('click', function (e) {
//       var irilabel = e.cell.value.replace(' * ','');
//       var labelStr = irilabel.split("/").pop();
      
//       var label1 = labelStr.split('_')[0];
//       var label2 = labelStr.split('_')[1];
//       $.ajax({
//         type : 'GET',
//         url : 'http://localhost:9000/hadatac/sddeditor_v2/getLabelFromIri',
//         data : {
//           iricode:irilabel
//         },
//         success : function(data) {
//           console.log(data);
//           console.log(label1+":"+data);
//         }
//       });
       
       
    
// });
}

function backToOriginal(){
  _gridcopy.style.display="none";
  _grid.style.display="block";
  _buttons.style.display="block";
  _footnote.style.display="block";
  
}

