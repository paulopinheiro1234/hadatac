
	function clearSearch(){
		var url = location.protocol + '//' + location.host + location.pathname;
		window.location.href = encodeURI(url);
	}

	function treeSelections(tree,pivot){
                pivotPref = '';
	        ck = tree.getAllChecked().split(',');
                if (ck.length == 0) { 
                    return null;
                } 
                var disj = [];
                //values = '';
                //window.alert(tree.getAllChecked());
	        for (i=0; i < ck.length; i++) {
                   var newValue = new Object;
                   newValue[tree.getUserData(ck[i], 'field')] = tree.getUserData(ck[i], 'value');
                   disj[i] = newValue;
	        }
                //window.alert(values);
                return disj;
        }

        function setCheckTree(tree, values, props) {
                if (values != null) {
                    values = values.substring(1,values.length - 1);
                    //window.alert("listStr : <" + values + ">");
                    if (values.length > 0) {
                       var list = values.split(",");
                       //window.alert("Value: " + list.length);
                       if (list != null && list.length > 0) { 
                           for (var i = 0; i < list.length; i++) {
                              list[i] = decodeURIComponent(list[i]).replace(/\&#x27;/gm,"'");
                              //window.alert("Tree item " + list[i]);
                              //window.alert("Tree pretty item " + facetPrettyName(props,list[i]));
                              var item = tree.findItem(facetPrettyName(props,list[i]));
                              tree.setCheck(item, true);
                              //window.alert("Tree item " + item + " " + list[i]);
                           }
                       }
                       //window.alert('Facets: ' + list + '  size: ' + list.length);
                    }
                }
        }

	function search(){
                //window.alert('@handler.toJSON()');
                var conj = new Object();
                var tmp = treeSelections(treeEC,"entity");
                if (tmp != null) { conj['facetsEC'] = tmp; }
	        tmp = treeSelections(treeS,"study_uri");
	        if (tmp != null) { conj['facetsS']  = tmp; }
	        tmp = treeSelections(treeU,"unit");
	        if (tmp != null) { conj['facetsU'] = tmp; }
	        tmp = treeSelections(treeT,"timestamp");
	        if (tmp != null) { conj['facetsT'] = tmp; }
	        tmp = treeSelections(treePI,"platform_name");
	        if (tmp != null) { conj['facetsPI'] = tmp; }
		//window.alert(JSON.stringify(jsonFacet));
                //window.alert(JSON.stringify(conj));
		var url = location.protocol + '//' + location.host + location.pathname;
	        //url += '?facets=' + JSON.stringify(jsonFacet);
	        url += '?facets=' + encodeURIComponent(JSON.stringify(conj));			       
		window.location.href = url;
	}




