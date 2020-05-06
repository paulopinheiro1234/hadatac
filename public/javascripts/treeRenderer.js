var query_res = document.getElementById('query'); 
var results = query_res.dataset.results;

var m = [20, 120, 20, 120],
    w = 1280 - m[1] - m[3],
    h = 600 - m[0] - m[2],
    i = 0,
    root;

var tree = d3.layout.tree()
    .size([h, w]);

var diagonal = d3.svg.diagonal()
    .projection(function(d) { return [d.y, d.x]; });

  root = JSON.parse(results);
  var depth = getDepth(root);
  console.log("Depth: " + depth);
  var theWidth = Math.max((depth*230), (w + m[1] + m[3]));

var vis = d3.select("#body").append("svg:svg")
    .attr("width", theWidth)
    .attr("height", h + m[0] + m[2])
    //.attr("style", "outline: thin solid lightsteelblue;")
  .append("svg:g")
    .attr("transform", "translate(" + m[3] + "," + m[0] + ")");

  //d3.json("http://mbostock.github.io/d3/talk/20111018/flare.json", function(json) {
  //alert(results);
  root.x0 = h / 2;
  root.y0 = 0;

  function toggleAll(d) {
    if (d.children) {
      d.children.forEach(toggleAll);
      toggle(d);
    }
  }

  // Initialize the display to show a few nodes.call scala template method from javascript
  //root.children.forEach(toggleAll);
  //toggle(root.children[1]);
  //toggle(root.children[1].children[2]);
  //toggle(root.children[9]);
  //toggle(root.children[9].children[0]);

  update(root);
//});

function getDepth(obj) {
    var depth = 0;
    if (obj.children) {
        obj.children.forEach(function (d) {
            var tmpDepth = getDepth(d)
            if (tmpDepth > depth) {
                depth = tmpDepth
            }
        })
    }
    return 1 + depth
}



function update(source) {
  var duration = d3.event && d3.event.altKey ? 5000 : 500;

  // Compute the new tree layout.
  var nodes = tree.nodes(root).reverse();

  //var max_x = d3.max(nodes, function(node) {return node.x}) + 4550;
  //var max_y = d3.max(nodes, function(node) {return node.y}) + 4550;
  
  //tree.size([max_y,max_x]);
  // Normalize for fixed-depth.
  nodes.forEach(function(d) { d.y = d.depth * 180; });

  // Update the nodes…
  var node = vis.selectAll("g.node")
      .data(nodes, function(d) { return d.id || (d.id = ++i); });

  // Enter any new nodes at the parent's previous position.
  var nodeEnter = node.enter().append("svg:g")
      .attr("class", "node")
      .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
      .on("click", function(d) { toggle(d); update(d); });

  nodeEnter.append("svg:circle")
      .attr("r", 1e-6)
      .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

  nodeEnter.append("svg:text")
      .attr("x", function(d) { return d.children || d._children ? -10 : 10; })
      .attr("dy", ".35em")
      .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
      .text(function(d) { return d.name; })
      .style("fill-opacity", 1e-6);

  // Transition nodes to their new position.
  var nodeUpdate = node.transition()
      .duration(duration)
      .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

  nodeUpdate.select("circle")
      .attr("r", 4.5)
      .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

  nodeUpdate.select("text")
      .style("fill-opacity", 1);
      
  // Transition exiting nodes to the parent's new position.
  var nodeExit = node.exit().transition()
      .duration(duration)
      .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
      .remove();

  nodeExit.select("circle")
      .attr("r", 1e-6);

  nodeExit.select("text")
      .style("fill-opacity", 1e-6);

  // Update the links…
  var link = vis.selectAll("path.link")
      .data(tree.links(nodes), function(d) { return d.target.id; });

  // Enter any new links at the parent's previous position.
  link.enter().insert("svg:path", "g")
      .attr("class", "link")
      .attr("d", function(d) {
        var o = {x: source.x0, y: source.y0};
        return diagonal({source: o, target: o});
      })
    .transition()
      .duration(duration)
      .attr("d", diagonal);

  // Transition links to their new position.
  link.transition()
      .duration(duration)
      .attr("d", diagonal);

  // Transition exiting nodes to the parent's new position.
  link.exit().transition()
      .duration(duration)
      .attr("d", function(d) {
        var o = {x: source.x, y: source.y};
        return diagonal({source: o, target: o});
      })
      .remove();

  // Stash the old positions for transition.
  nodes.forEach(function(d) {
    d.x0 = d.x;
    d.y0 = d.y;
  });
}

// Toggle children.
function toggle(d) {
  if (d.children) {
    d._children = d.children;
    d.children = null;
  } else {
    d.children = d._children;
    d._children = null;
  }
}

function expand(d){   
    var children = (d.children)?d.children:d._children;
    if (d._children) {        
      d.children = d._children;
      d._children = null;       
    }
    if(children) {
      children.forEach(expand);
    }
}

function collapse(d) {
    if (d.children) {
      d._children = d.children;
      d._children.forEach(collapse);
      d.children = null;
    }
}

function collapseBelowLevel(d, l, limit) {
	if (l < limit) {
		var newL = l + 1;
		if (d.children) {
			var i;
			for (i = 0; i < d.children.length; i++) {
			    collapseBelowLevel(d.children[i], newL, limit);   
			}
		}
	} else {
		if (d.children) {
			d.children.forEach(collapse);
		}
	}
}

function expandAll(){
   expand(root); 
   update(root);
}

function collapseAll(){
   root.children.forEach(collapse);
   collapse(root);
   update(root);
}

function initialState(){
	expand(root);
	if (root.children) {
	   var i;
	   for (i = 0; i < root.children.length; i++) {
	       collapseBelowLevel(root.children[i], 0, 1);   
	   }
   }
   update(root);
}


