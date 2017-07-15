!(function (d3) {

$("treecontent").empty();

var query_res = document.getElementById('entityquery'); 
var results = query_res.dataset.results;

var margin = {top: 30, right: 20, bottom: 30, left: 20},
    width = 960 - margin.left - margin.right,
    barHeight = 20,
    barWidth = width * .8;

var i = 0,
    duration = 400,
    root;

var tree = d3.layout.tree()
    .nodeSize([0, 20]);

var diagonal = d3.svg.diagonal()
    .projection(function(d) { return [d.y, d.x]; });

var svg = d3.select("treecontent").append("svg")
    .attr("width", width + margin.left + margin.right)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

function collapse(d) {
   if (d.children) {
      d._children = d.children;
      d._children.forEach(collapse);
      d.children = null;
   }
}

function expand(d){   
    var children = (d.children)?d.children:d._children;
    if (d._children) {        
        d.children = d._children;
        d._children = null;       
    }
    if(children)
	children.forEach(expand);
}

var flare = JSON.parse(results);
    flare.x0 = 0;
    flare.y0 = 0;
    flare.children.forEach(collapse);
    update(root = flare);

function update(source) {

    // Compute the flattened node list. TODO use d3.layout.hierarchy.
    var nodes = tree.nodes(root);

    var height = Math.max(500, nodes.length * barHeight + margin.top + margin.bottom);

    d3.select("svg").transition()
	.duration(duration)
	.attr("height", height);

    // Compute the "layout".
    nodes.forEach(function(n, i) {
	    n.x = i * barHeight;
	});

    // Update the nodes…
    var node = svg.selectAll("g.node")
	.data(nodes, function(d) { return d.id || (d.id = ++i); });

    var nodeEnter = node.enter().append("g")
	.attr("class", "node")
	.attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
	.style("opacity", 1e-6);

    // Enter any new nodes at the parent's previous position.
    nodeEnter.append("rect")
	.attr("y", -barHeight / 2)
	.attr("height", barHeight)
	.attr("width", barWidth)
	.style("fill", color)
	.on("click", click);

    nodeEnter.append("text")
	.attr("dy", 3.5)
	.attr("dx", 5.5);
      
    node.select('text')
	.text(function(d) { 
		if (d.children) {
		    return '+' + d.name;
		} else if (d._children) {
		    return '-' + d.name;
		} else {
		    return d.name;
		}
	    });

    // Transition nodes to their new position.
    nodeEnter.transition()
	.duration(duration)
	.attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
	.style("opacity", 1);

    node.transition()
	.duration(duration)
	.attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
	.style("opacity", 1)
	.select("rect")
	.style("fill", color);

    // Transition exiting nodes to the parent's new position.
    node.exit().transition()
	.duration(duration)
	.attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
	.style("opacity", 1e-6)
	.remove();

    // Update the links…
    var link = svg.selectAll("path.link")
	.data(tree.links(nodes), function(d) { return d.target.id; });

    // Enter any new links at the parent's previous position.
    link.enter().insert("path", "g")
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

// Toggle children on click.
var lastClickD = null;
document.getElementById('copyvalue').disabled = true;
function click(d) {
    if (d.children) {
	d._children = d.children;
	d.children = null;
    } else {
	d.children = d._children;
	d._children = null;
    }
    if (lastClickD){
	lastClickD._isSelected = false;
    }
    d._isSelected = true;
    lastClickD = d;
    document.getElementById('copyvalue').disabled = false;
    update(d);
}

document.getElementById("copyvalue").onclick = function() {replaceEntityValue()};

function replaceEntityValue() {
    $('#newEntity').val(lastClickD.name);
}

document.getElementById("collapse").onclick = function() { 
    root.children.forEach(collapse);
    update(root);
};

document.getElementById("expand").onclick = function() { 
    expand(root);
    update(root);
};

function color(d) {
    if (d._isSelected) return 'red';
    return d._children ? "#3182bd" : d.children ? "#c6dbef" : "#fd8d3c";
}

})(d3);