var query_res = document.getElementById('query');
var results = query_res.dataset.documents;
var json = JSON.parse(results);

function getURLParameter(name) {
	return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20'))||null
}