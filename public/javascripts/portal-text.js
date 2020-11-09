function showText(msg,tagId) {
    var doc = new DOMParser().parseFromString(msg, "text/html");
    var msgHTML = document.getElementById(tagId).innerHTML;
    document.getElementById(tagId).innerHTML = msgHTML + doc.body.innerText;
}