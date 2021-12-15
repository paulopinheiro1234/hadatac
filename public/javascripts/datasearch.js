
function treeSelections(tree, id) {
	var disj = [];
	var subitems = tree.getSubItems(id);
	if (subitems.length > 0) {
		subitems.split(",").forEach(function(element) {
			if (tree.isItemChecked(element)) {
				if (tree.getUserData(element, 'facet') == undefined) {
					// console.log("tree.getUserData(element, 'facet') == undefined");
					var pair = new Object();
					pair['id'] = tree.getUserData(element, 'value');
					pair[tree.getUserData(element, 'field')] = tree.getUserData(element, 'value');		
					if (tree.hasChildren(element)) {
						pair['children'] = treeSelections(tree, element);
					}
					disj.push(pair);
				} else {
					disj.push(tree.getUserData(element, 'facet'));
				}
			}
		});
	} else {
		return null;
	}
	
	return disj;
}

function getSelectedFacets() {
	var facets = new Object();
	var tmp;
	tmp = treeSelections(treeEC, 0);
	if (tmp != null) {
		facets['facetsEC'] = tmp;
	}
	tmp = treeSelections(treeS, 0);
	if (tmp != null) {
		facets['facetsS']  = tmp;
	}
	tmp = treeSelections(treeOC, 0);
	if (tmp != null) {
		facets['facetsOC'] = tmp;
	}
	tmp = treeSelections(treeU, 0);
	if (tmp != null) {
		facets['facetsU'] = tmp;
	}
	tmp = treeSelections(treeT, 0);
	if (tmp != null) {
		facets['facetsT'] = tmp;
	}
	tmp = treeSelections(treePI, 0);
	if (tmp != null) {
		facets['facetsPI'] = tmp;
	}
	
	console.log("facets: " + JSON.stringify(facets));
	return JSON.stringify(facets);
}

function search() {
	var facets = getSelectedFacets();
	console.log("selected facets: " + facets);
	$.redirect(location.pathname, {'facets': facets});
}

function showData(page) {
	$.redirect(location.pathname + '/data?start=' + page, {'facets': getSelectedFacets()});
}

function goToPage(page) {
	$.redirect(location.pathname + '?start=' + page, {'facets': getSelectedFacets()});
}

function hideData(page) {
	$.redirect(location.pathname.replace('/data', ''), {'facets': getSelectedFacets()});
}

function clearSearch() {
	var url = location.protocol + '//' + location.host + location.pathname;
	window.location.href = encodeURI(url);
}

function openShiny() {
	var url = location.protocol + '//' + location.hostname + ':8081';
	window.open(encodeURI(url), '_blank');
}