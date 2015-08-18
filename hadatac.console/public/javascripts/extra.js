function info_on(id) {
    var e = document.getElementById(id);
    if(e.style.display == 'none')
        e.style.display = 'block';
}
    
function info_off(id){
    var e = document.getElementById(id);
    if(e.style.display == 'block')
        e.style.display = 'none';
}