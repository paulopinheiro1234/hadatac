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

var tree_id = 0;
function create_item(data, selected_elems) {
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
		if (selected_elems.indexOf(element.tooltip) > -1) {
			//element.checked = 1;
		}
		element.item = create_item(children[i_child], selected_elems);
		for (var i = 0; i < element.item.length; i++) {
			if (element.item[i].checked == 1) {
				element.open = "yes";
				break;
			}
		}
		item.push(element);
	}
	
	return item;
}

function parseSolrFacetToTree(facet_name, selected_elems) {
	console.log("facet_name: " + facet_name);
	console.log("selected_elems: " + selected_elems);
	dataTree = {};
	tree_id = 0;
	dataTree.id = tree_id++;
	items = create_item(json.extra_facets[facet_name], selected_elems);
	if (null == items) {
		items = [];
	}
	dataTree.item = items;
	return dataTree;
}


