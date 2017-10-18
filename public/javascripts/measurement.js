var query_res = document.getElementById('query');
var results = query_res.dataset.documents;
var json = JSON.parse(results);
var facet_res = document.getElementById('facetDiv');
var facetsStrFromDiv = facet_res.dataset.documents;
var jsonFacet = JSON.parse(facetsStrFromDiv);

function getURLParameter(name) {
	return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20'))||null
}

function facetPrettyName(type, value) {
    switch(type) {
        case 'study_uri':
            if (value.indexOf(":") > 0) {
	       value = value.substring(value.indexOf(":") + 1);
	    }
            break;
        case 'acquisition_uri':
            if (value.indexOf("#") > 0) {
               value = value.substring(value.indexOf("#") + 1);
            }
            break;
        case 'study_uri,acquisition_uri':
            if (value.indexOf("#") > 0) {
               value = value.substring(value.indexOf("#") + 1);
            } else {
               if (value.indexOf(":") > 0) {
	          value = value.substring(value.indexOf(":") + 1);
	       }
	    }
            break;
        case 'entity':
        	value += "'s attribute";
            break;
    }
    return value;
}

function parseSolrRangeFacetFieldToTree(type) {
	var i;
	i = 0;
        listed = 0;
	flag = false;
	jsonTree = '{ "id": ' + i + ', "item": [ ';
	for (var i_field in json.date_facets[type]) {
		i++;
                	jsonTree += '{	"id": ' + i + ', ' +
                				   '"userdata": [ ';
                	/*
	       	      jsonTree += '{ "id": ' + i + ', ' + 
                                    '"userdata": [ { "name": "field", "content": "' + type + '" }, { "name": "value", "content": "' + i_field + '" } ], ' + 
			  '"text": "' + facetPrettyName(type,i_field) + ' (' + field + ')" } ';
			        */
                	jsonTree += ' ], "text" : "' + i_field + '" } ,';
	}
	if (jsonTree[jsonTree.length-1] == ',') {
		jsonTree = jsonTree.substring(0, jsonTree.length-1);
	}
	jsonTree += '] }';
	return jsonTree;
}

function parseSolrFacetFieldToTree(type) {
	var i;
	i = 0;
        listed = 0;
	flag = false;
	jsonTree = '{ "id": ' + i + ', "item": [ ';
	for (var i_field in json.field_facets[type]) {
		var field = json.field_facets[type][i_field];
                if (field > 0) {
                    listed++;
		}
		//if (i != 0) {
                if ((listed > 1) && (field > 0)) {
			jsonTree += ' ,';
		}
		i++;
                if (field > 0) {
	       	      jsonTree += '{ "id": ' + i + ', ' + 
                                    '"userdata": [ { "name": "field", "content": "' + type + '" }, { "name": "value", "content": "' + i_field + '" } ], ' + 
			  '"text": "' + facetPrettyName(type,i_field) + ' (' + field + ')" } ';
                }
	}
	jsonTree += '] }';
	return jsonTree;
}

function parseSolrFacetPivotToTree(type) {
	var i, j, q, n, jsonTree, fields;
	i = 0;
	n = 0;
	jsonTree = '{ "id": ' + n + ', "item": [ ';
	fields = type.split(",");
	
	for (var i_pivot in json.pivot_facets[type]) {
		var field1 = json.pivot_facets[type][i_pivot];
		j = 0;
		
		if (i != 0) {
			jsonTree += ' ,';
		}
		i++;
		jsonTree += '{ "id": ' + ++n + ', "text": "' + facetPrettyName(fields[0],field1.value) + ' (' + field1.count + ')"';
		jsonTree += ', "item": [ ';
		for (var j_pivot in field1.children) {
			var field2 = field1.children[j_pivot];
			if (j != 0) {
				jsonTree += ' ,';
			}
			j++;
			jsonTree += '{ "id": ' + ++n + ', "text": "' + facetPrettyName(fields[1],field2.value) + ' (' + field2.count + ')", ' + 
                                      '"tooltip": "' + field2.value + '" , ' + 
                                      '"userdata": [ { "name": "field", "content": "' + fields[1] + '" }, ' + 
                                      '{ "name": "value", "content": "' + field2.value + '" } ] } ';
		}
		jsonTree += '] , "child": ' + j + ', "tooltip": "' + field1.value + '" , ' + 
                                '"userdata": [ { "name": "field", "content": "' + fields[0] + '" }, ' + 
                                '{ "name": "value", "content": "' + field1.value + '" } ] } ';
	}
	jsonTree += '] }';
	return jsonTree;
}

var tree_id = 0;
function create_item(data) {
	if (null == data) {
		return;
	}
	var item = [];
	var children = data.children;
	for (var i_child in children) {
		var element = {};
		element.id = tree_id;
		tree_id++;
		element.text = facetPrettyName(data.field, children[i_child].value) + ' (' + children[i_child].count + ')';
		element.tooltip = children[i_child].tooltip;
		element.userdata = [{"name": "field", "content": children[i_child].field},
			{"name": "value", "content": children[i_child].tooltip}];
		element.item = create_item(children[i_child]);
		item.push(element);
	}
	
	return item;
}

function parseSolrFacetToTree(facet_name) {
	dataTree = {};
	tree_id = 0;
	dataTree.id = tree_id++;
	items = create_item(json.extra_facets[facet_name]);
	if (null == items) {
		items = [];
	}
	dataTree.item = items;
	return dataTree;
}


