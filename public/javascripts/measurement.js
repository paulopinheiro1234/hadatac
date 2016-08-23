var query_res = document.getElementById('query');
var results = query_res.dataset.documents;
var json = JSON.parse(results);
var facet_res = document.getElementById('facetDiv');
var facetsStrFromDiv = facet_res.dataset.documents;
var jsonFacet = JSON.parse(facetsStrFromDiv);

function getURLParameter(name) {
	return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20'))||null
}

function parseSolrFacetFieldToTree(type) {
	var i;
	i = 0;
	flag = false;
	jsonTree = '{ "id": ' + i + ', "item": [ ';
	for (var i_field in json.field_facets[type]) {
		var field = json.field_facets[type][i_field];
		if (i != 0) {
			jsonTree += ' ,';
		}
		i++;
		jsonTree += '{ "id": ' + i + ', "userdata": [ { "name": "field", "content": "' + type + '" }, { "name": "value", "content": "' + i_field + '" } ], "text": "' + i_field + ' (' + field + ')" } ';
	}
	jsonTree += '] }';
	//alert(jsonTree);
	return jsonTree;
}

function parseSolrFacetPivotToTree(type) {
	var i, j, q, jsonTree, fields;
	i = 0;
	jsonTree = '{ "id": ' + i + ', "item": [ ';
	fields = type.split(",");
	
	for (var i_pivot in json.pivot_facets[type]) {
		var field1 = json.pivot_facets[type][i_pivot];
		j = 0;
		
		if (i != 0) {
			jsonTree += ' ,';
		}
		i++;
		jsonTree += '{ "id": ' + i + ', "text": "' + field1.value + ' (' + field1.count + ')"';
		jsonTree += ', "item": [ ';
		for (var j_pivot in field1.children) {
			var field2 = field1.children[j_pivot];
			if (j != 0) {
				jsonTree += ' ,';
			}
			j++;
			jsonTree += '{ "id": ' + i + j + ', "text": "' + field2.value + ' (' + field2.count + ')", "tooltip": "' + field2.value + '" , "userdata": [ { "name": "field", "content": "' + fields[1] + '" }, { "name": "value", "content": "' + field2.value + '" } ] } ';
		}
		jsonTree += '] , "child": ' + j + ', "tooltip": "' + field1.value + '" , "userdata": [ { "name": "field", "content": "' + fields[0] + '" }, { "name": "value", "content": "' + field1.value + '" } ] } ';
	}
	jsonTree += '] }';
	//alert(jsonTree);
	return jsonTree;
}