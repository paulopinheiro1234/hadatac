var current_page = 1;
var records_per_page = 3;
var numToPage;
var listOfLabelAndIRI=[]
function showTop(){
    var e = document.getElementById("numToSearch");
    numToPage = e.value;
}

function returnToView(){
    document.getElementById("irifound").innerHTML = "";
    document.getElementById("recommendedterms").style.display="block";
    document.getElementById("recommendedcolumn").style.display="block";
    document.getElementById("returnView").style.display="none";
    document.getElementById("menulist").style.display="none";
    document.getElementById("virtuallist").style.display="none";
    document.getElementById("irifound").style.display="none";
    document.getElementById("listingTable").style.display="none";
    document.getElementById("termToSearch").value="";
    document.getElementById("numberResults").innerHTML="";
    document.getElementById("pagekey").style.display="none";
    document.getElementById("numToSearch").style.display="block";
    document.getElementById("searchForTerm").style.display="block";

}
var searchItem=document.getElementById("searchOnt");
searchItem.addEventListener('click', function(e) {
    if(document.getElementById("numberResults").innerHTML!=""){
        document.getElementById("numberResults").innerHTML="";
    }
    document.getElementById("btn_next").style.visibility = "hidden";
    document.getElementById("btn_prev").style.visibility = "hidden";
    var btn_prev = document.getElementById("btn_prev");
    document.getElementById("recommendedterms").style.display="none";
    document.getElementById("recommendedcolumn").style.display="none";
    document.getElementById("returnView").style.display="block";
    var termToSearch=document.getElementById("termToSearch").value;
    console.log(termToSearch);
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


    $.ajax({
      type : 'GET',
      // url : 'http://localhost:9000/hadatac/sddeditor_v2/getBioportalKey',
      url : '/hadatac/sddeditor_v2/getBioportalKey',
      data : {
          //editValue: changeValue
      },
      success : function(data) {
         bioportal_key=data;


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


                 var ontoCartName=contact.collection[i].links.ontology;
                 var ontoName=contact.collection[i]["@id"];



                 eachEntry.push(prefLabel_);
                 eachEntry.push(def);
                 eachEntry.push(ontoName);
                 eachEntry.push(ontoCartName);

                 resultsArray.push(eachEntry);

             }


             createDisplay(resultsArray,numResults);

         });

      }
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
            var cartonto=resultsArray[i][3];


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
            entryItem.cartontoname=cartonto;
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
    listOfLabelAndIRI=[];
    if (current_page > 1) {
        current_page--;
        changePage(current_page,copyObjJson);
    }
}

function nextPage()
{
    listOfLabelAndIRI=[];
    if (current_page < numPages(copyObjJson)) {
        console.log("here")
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

    clickCoords = getPosition(e);
    clickCoordsX = clickCoords.x;

    clickCoordsY = clickCoords.y;


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


function changePage(page,objJson){
    console.log("reched")
    var btn_next = document.getElementById("btn_next");
    var btn_prev = document.getElementById("btn_prev");
    var listing_table = document.getElementById("listingTable");
    listing_table.style.display="block";
    var page_span = document.getElementById("page");
    // Validate page
    if (page < 1) page = 1;
    if (page > numPages(objJson)) page = numPages(objJson);

    listing_table.innerHTML = "";
    var tabchar="-";

    for (var i = (page-1) * records_per_page; i < (page * records_per_page) && i < objJson.length; i++) {
        var num=i+1;
        listing_table.style.background = "AliceBlue";
        parts= objJson[i].cartontoname.split('/');
        reference = parts.pop();
        var tempLIRI=[];
        var prefix;
        if(objJson[i].ontolink.includes("#")){

            prefix=reference;
        }
        else if(!objJson[i].ontolink.includes("#")){
            var ontologypart=objJson[i].ontolink.split("/").pop()
            prefix=ontologypart.split('_')[0];
        }
        termdict.set(objJson[i].termname,reference)
        //listing_table.innerHTML += num+") "
        var element = document.createElement("button");
        element.style.backgroundColor="lavender";
        element.style.color="black";
        element.style.border="transparent";
        element.style.border="0";
        element.style.width="100%";
        element.style.fontFamily="Optima, sans-serif";
        element.style.fontsize="12pt";
        element.style.fontWeight="bold";

        element.innerHTML=objJson[i].termname;
        tempLIRI.push(prefix+":"+objJson[i].termname);

        listing_table.appendChild(element);
        var des = document.createElement("button");
        des.style.backgroundColor="AliceBlue";
        des.style.color="black";
        des.style.border="transparent";
        des.style.border="0";
        des.style.width="100%";
        des.style.fontFamily="Gill Sans, sans-serif";
        des.style.fontsize="8pt";
        des.style.textAlign="left";
        des.style.wordWrap="break-word";
        des.style.wordBreak="keep-all";
        des.style.padding="0px";
        var shortenedDes=returnWordCt(objJson[i].description);
        des.innerHTML="-Description: "+shortenedDes;


        var ont = document.createElement("button");
        ont.style.backgroundColor="AliceBlue";
        ont.style.color="black";
        ont.style.border="transparent";
        ont.style.border="0";
        ont.style.width="100%";
        ont.style.fontFamily="Gill Sans, sans-serif";
        ont.style.fontsize="8pt";
        ont.style.textAlign="left";
        ont.style.wordWrap="break-word";
        ont.style.wordBreak="keep-all";
        ont.style.padding="0px";
        ont.innerHTML=objJson[i].ontolink;
        tempLIRI.push(objJson[i].ontolink);
        listOfLabelAndIRI.push(tempLIRI);

        if (document.addEventListener) { // IE >= 9; other browsers
            ont.addEventListener('contextmenu', function(e) {
                termChosen= this.innerHTML;

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


        if (document.addEventListener) { // IE >= 9; other browsers
            ont.addEventListener('click', function(e) {
                linkChosen= this.innerHTML;
                var win = window.open(linkChosen, '_blank');
                win.focus();



            }, false);

         }
         else { // IE < 9
            document.attachEvent('oncontextmenu', function() {
               alert("You've tried to open context menu");
               window.event.returnValue = false;
            });
         }
        listing_table.appendChild(des);
        listing_table.appendChild(ont);


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

        stringToPass=termChosen;
        $.ajax({
            type : 'GET',

            // url : 'http://localhost:9000/hadatac/sddeditor_v2/addToCart',
            url : '/hadatac/sddeditor_v2/addToCart',
            data : {
                ontology: stringToPass

            },
            success : function(data) {
                addcartlocal();
                //createMenu(ct);
            }
        });



    }, false);

    function returnWordCt(passage){
        var toRet;
        var passageList=passage.split(' ');
        if(passageList.length >= 25){
            passageList.splice(25)
            toRet=passageList.join(" ");
            toRet+=" ... ";
        }
        else{
            toRet=passage;
        }

        return toRet;
    }
