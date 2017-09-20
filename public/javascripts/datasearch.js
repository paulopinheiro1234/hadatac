
function clearSearch() {
	var url = location.protocol + '//' + location.host + location.pathname;
	window.location.href = encodeURI(url);
}

function treeSelections(tree) {
	var disj = [];
	var checked_items = tree.getAllChecked();
	if (checked_items.length > 0) {
		checked_items.split(",").forEach(function(element) {
			if (tree.getAllSubItems(element).length == 0) {
				var pair = new Object();
				pair[tree.getUserData(element, 'field')] = tree.getUserData(element, 'value');
				disj.push(pair);
			}
		});	
	} else {
		return null;
	}
	
	return disj;
}

function setCheckTree(tree, values, props) {
	if (values != null) {
		values = values.substring(1,values.length - 1);
		if (values.length > 0) {
			var list = values.split(",");
			if (list != null && list.length > 0) { 
				for (var i = 0; i < list.length; i++) {
					list[i] = decodeURIComponent(list[i]).replace(/\&#x27;/gm, "'");
					var item = tree.findItem(facetPrettyName(props,list[i]));
					tree.setCheck(item, true);
				}
			}
		}
	}
}

function search() {
	var conj = new Object();
	var tmp = treeSelections(treeEC);
	if (tmp != null) {
		conj['facetsEC'] = tmp;
	}
	tmp = treeSelections(treeS);
	if (tmp != null) {
		conj['facetsS']  = tmp;
	}
	tmp = treeSelections(treeU);
	if (tmp != null) {
		conj['facetsU'] = tmp;
	}
	tmp = treeSelections(treeT);
	if (tmp != null) {
		conj['facetsT'] = tmp;
	}
	tmp = treeSelections(treePI);
	if (tmp != null) {
		conj['facetsPI'] = tmp;
	}
	console.log("conj: " + conj);	       
	window.location.href = location.protocol + '//' + location.host + location.pathname + '?facets=' + encodeURIComponent(JSON.stringify(conj));
}




