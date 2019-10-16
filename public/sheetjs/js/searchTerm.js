var current_page = 1;
var records_per_page = 4;
var numToPage;
function showTop(){
    //numToPage=document.getElementById('numToSearch').value;
    var e = document.getElementById("numToSearch");
    console.log(e.value);
    numToPage = e.value;
}
    //$('#searchOnt').prop('disabled', false);

function returnToView(){
    document.getElementById("irifound").innerHTML = "";
    document.getElementById("recommendedterms").style.display="block";
    document.getElementById("recommendedcolumn").style.display="block";
    document.getElementById("editRowsAdd").style.display="block";
    document.getElementById("verifysdd").style.display="block";
    document.getElementById("returnView").style.display="none";
    document.getElementById("listingTable").innerHTML="";
    document.getElementById("termToSearch").innerHTML="";
    document.getElementById("numberResults").innerHTML="";
    document.getElementById("pagekey").style.display="none";
    document.getElementById("numToSearch").style.display="block";
    document.getElementById("searchForTerm").style.display="block";
    //document.getElementById("btn_prev").style.display="none";
}
var searchItem=document.getElementById("searchOnt");
searchItem.addEventListener('click', function(e) {
    document.getElementById("listingTable").innerHTML="";
    document.getElementById("numberResults").innerHTML="";
    document.getElementById("recommendedterms").style.display="none";
    document.getElementById("recommendedcolumn").style.display="none";
    document.getElementById("editRowsAdd").style.display="none";
    document.getElementById("verifysdd").style.display="none";
    document.getElementById("returnView").style.display="block";
    var termToSearch=document.getElementById("termToSearch").value;
    prehttp(termToSearch);
});

var HttpClient = function() {
    this.get = function(aUrl, aCallback) {
        var anHttpRequest = new XMLHttpRequest();
        anHttpRequest.open("GET", aUrl, true);
        anHttpRequest.onreadystatechange = function() { 
            if (anHttpRequest.readyState == 4 && anHttpRequest.status == 200)
                aCallback(anHttpRequest.responseText);
        }

        anHttpRequest.open( "GET", aUrl, true );            
        anHttpRequest.send( null );
    }
}
function prehttp(termToSearch){
    
    var resultsArray=[];
    var client = new HttpClient();
    var theUrl;
    var bioportal_key="";
    console.log(numToPage)
    $.ajax({
        type : 'GET',
        url : 'http://localhost:9000/hadatac/annotator/sddeditor_v2/getBioportalKey',
        data : {
           //editValue: changeValue
        },
        success : function(data) {
          bioportal_key=data;
    
        }
      });
    

    if(numToPage=="All"){
        theUrl="http://data.bioontology.org/search?q="+termToSearch+"&apikey="+bioportal_key;
    }
    else if(numToPage==null){
        numToPage=5;
        theUrl="http://data.bioontology.org/search?q="+termToSearch+"&apikey="+bioportal_key+"&pagesize="+numToPage
    }
    else{
        theUrl="http://data.bioontology.org/search?q="+termToSearch+"&apikey="+bioportal_key+"&pagesize="+numToPage;
    }
    
    //var theUrl="http://data.bioontology.org/search?q="+termToSearch+"&apikey=3b6101b1-fc1a-45c2-a8a6-136a04f228c5&pagesize=5";
    client.get(theUrl, function(response) {
        
        let contact = JSON.parse(response);
        var numResults=contact.collection.length;
        for(var i=0;i<contact.collection.length;i++){
            var eachEntry=[];
            var prefLabel_=contact.collection[i].prefLabel;
            var def;
            
            if(contact.collection[i].definition!=null){
                def=contact.collection[i].definition[0];
            }
            else{
                def="";
            }
            
          
            var ontoName=contact.collection[i].links.ontology;
            eachEntry.push(prefLabel_);
            eachEntry.push(def);
            eachEntry.push(ontoName);
            
            resultsArray.push(eachEntry);

        }
        
        
        createDisplay(resultsArray,numResults);
       
    });
}
var copyObjJson;
function createDisplay(resultsArray,numResults){

    var objJson=[];
    
    for(var i=0;i<resultsArray.length;i++){
            var entryItem={};
            var name=resultsArray[i][0];
            var des=resultsArray[i][1];
            var onto=resultsArray[i][2];
            // item_={"termname":name,
            //         "description":des,
            //         "ontologylink":onto}
            
            entryItem.termname=name;
            if(des==""){
                
                des="n/a";
                entryItem.description=des;
                
            }
            else{
               
                entryItem.description=des;
                //des="n/a";
            }
            
            entryItem.ontolink=onto;
            objJson.push(entryItem);
        
       
    }
    
    
   
     copyObjJson=objJson;
    numRes(numResults);
     var pgekey = document.getElementById("pagekey");
    pgekey.style.display="block";
    changePage(1,objJson);
    
    

}

function prevPage()
{
    if (current_page > 1) {
        current_page--;
        changePage(current_page,copyObjJson);
    }
}

function nextPage()
{
    if (current_page < numPages(copyObjJson)) {
        current_page++;
        changePage(current_page,copyObjJson);
    }
}
function numRes(numResults){
    document.getElementById("numberResults").style.color = 'green';
    document.getElementById("numberResults").style.float = 'left';
    document.getElementById("numberResults").innerHTML+=numResults+" Results Found"
}


var cm=document.querySelector(".custom-cm");
function showContextMenu(show=true){
    cm.style.display=show ?'block' :"none";
}
var clickCoords;
var clickCoordsX;
var clickCoordsY;
var menuState = 0;
var menuWidth;
var menuHeight;
var menuPosition;
var menuPositionX;
var menuPositionY;

var windowWidth;
var windowHeight;
function getPosition(e) {
    var posx = 0;
    var posy = 0;

    if (!e) var e = window.event;

    if (e.pageX || e.pageY) {
    posx = e.clientX;
    posy = e.clientY;
    } else if (e.clientX || e.clientY) {
    posx = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
    posy = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
    }

    return {
    x: posx,
    y: posy
    }
}
function positionMenu(e) {
    console.log("reached2")
    clickCoords = getPosition(e);
    clickCoordsX = clickCoords.x;

    clickCoordsY = clickCoords.y;
console.log(clickCoordsX,clickCoordsY)

    menuWidth = cm.offsetWidth + 4;
    menuHeight = cm.offsetHeight + 4;

    windowWidth = window.innerWidth;
    windowHeight = window.innerHeight;

    if ( (windowWidth - clickCoordsX) < menuWidth ) {
        cm.style.left = windowWidth - menuWidth + "px";
    } else {
        cm.style.left = clickCoordsX + "px";
    }

    if ( (windowHeight - clickCoordsY) < menuHeight ) {
        cm.style.top = windowHeight - menuHeight + "px";
    } else {
        cm.style.top = clickCoordsY + "px";

    }

    }


 var termChosen;

var termdict=new Map();

function callFunction(vari){
    alert(vari);
}
function changePage(page,objJson)
{
 
    var btn_next = document.getElementById("btn_next");
    var btn_prev = document.getElementById("btn_prev");
    var listing_table = document.getElementById("listingTable");
    var page_span = document.getElementById("page");
    // Validate page
    if (page < 1) page = 1;
    if (page > numPages(objJson)) page = numPages(objJson);

    listing_table.innerHTML = "";
    var tabchar="-";
    var colors_background=["gainsboro","whitesmoke"];
    for (var i = (page-1) * records_per_page; i < (page * records_per_page) && i < objJson.length; i++) {
        var num=i+1;
        listing_table.style.background = "AliceBlue";
        parts= objJson[i].ontolink.split('/');
        reference = parts.pop();
        
        termdict.set(objJson[i].termname,reference)
        //listing_table.innerHTML += num+") "
        var element = document.createElement("button");
        element.style.backgroundColor="lavender";
        element.style.color="black";
        element.style.border="transparent";
        element.style.border="0";
        element.style.width="350px";
        element.style.fontFamily="Optima, sans-serif";
        element.style.fontsize="12pt";
        element.style.fontWeight="bold";
        
        element.innerHTML=objJson[i].termname;  

        if (document.addEventListener) { // IE >= 9; other browsers
            element.addEventListener('contextmenu', function(e) {
                termChosen= termdict.get(this.innerHTML)+": "+this.innerHTML;
              
               e.preventDefault();
                showContextMenu();
                positionMenu(e);
     
            }, false);
            document.addEventListener("click",function(e){
             showContextMenu(false);
            });
            document.addEventListener("scroll",function(e){
             showContextMenu(false);
            });
         } 
         else { // IE < 9
            document.attachEvent('oncontextmenu', function() {
               alert("You've tried to open context menu");
               window.event.returnValue = false;
            });
         }

      
        listing_table.appendChild(element);
        var des = document.createElement("button");
        des.style.backgroundColor="AliceBlue";
        des.style.color="black";
        des.style.border="transparent";
        des.style.border="0";
        des.style.width="350px";
        des.style.fontFamily="Optima, sans-serif";
        des.style.fontsize="11pt";
        des.style.textAlign="left";
        des.innerHTML="- Decription: "+objJson[i].description;   
        
       
        var ont = document.createElement("button");
        ont.style.backgroundColor="AliceBlue";
        ont.style.color="black";
        ont.style.border="transparent";
        ont.style.border="0";
        ont.style.width="350px";
        ont.style.fontFamily="Optima, sans-serif";
        ont.style.fontsize="11pt";
        ont.style.textAlign="left";
        ont.innerHTML="- "+objJson[i].ontolink;   
        ont.addEventListener('click',function(){
            location.href=objJson[i].ontolink
        })
        listing_table.appendChild(des);
        listing_table.appendChild(ont);
        //listing_table.innerHTML += objJson[i].termname
    }
    
    page_span.innerHTML = page + "/" + numPages(objJson);

    if (page == 1) {
        btn_prev.style.visibility = "hidden";
    } else {
        btn_prev.style.visibility = "visible";
    }

    if (page == numPages(objJson)) {
        btn_next.style.visibility = "hidden";
    } else {
        btn_next.style.visibility = "visible";
    }
}

function numPages(objJson)
{
    
    return Math.ceil(objJson.length / (records_per_page));
}

var additem=document.getElementById("thisitem");
    additem.addEventListener('click', function(e) {
        var str= termChosen;
        // var ul = document.getElementById("seecart");
        // var li = document.createElement("li");
        // li.appendChild(document.createTextNode(str));
        // ul.appendChild(li);
        $.ajax({
            type : 'GET',

            url : 'http://localhost:9000/hadatac/annotator/sddeditor_v2/addToCart',
            data : {
                ontology: str
            },
            success : function(data) {
                addcartlocal();
                //createMenu(ct);
            }
        });



    }, false);
