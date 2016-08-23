var query_res = document.getElementById('query');
var results = query_res.dataset.results;

var width = 1280,
    height = 760;

var color = d3.scale.category20();

var force = d3.layout.force()
    .size([width, height])
    .charge(-400)
    .linkDistance(100)
    .on("tick", tick);

var drag = force.drag()
    .on("dragstart", dragstart);

// This is the tooltip!
/*var tip = d3.tip()
  .attr('class', 'd3-tip')
  .offset([-10, 0])
  .html(function(d) {
    return "<strong>Name:</strong> <span style='color:black'>" + d.name + "</span><br>" +
           "<strong>Description:</strong> <span style='color:black'>" + " " + "</span><br>" + 
           "<strong>e-Mail:</strong> <span style='color:black'>" + d.email + "</span><br>"  +
           "<strong>website:</strong> <span style='color:black'>" + " " + "</span><br>";  
           
  })
*/

var svg = d3.select("#body").append("svg")
    .attr("width", width)
    .attr("height", height);

//svg.call(tip);

var link = svg.selectAll(".link"),
    node = svg.selectAll(".node");

//d3.json("graph.json", function(error, graph) {
//  if (error) throw error;

//alert(results);

graph=JSON.parse(results);

force
    .nodes(graph.nodes)
    .links(graph.links)
    .start();

link = link.data(graph.links)
    .enter().append("line")
    .attr("class", "link");

node = node.data(graph.nodes)
    .enter().append("circle")
    .attr("class", "node")
    .attr("r", 12)
    .style("fill", function(d) { return color(d.group); })
    .on("dblclick", dblclick)
    //.on('mouseover', tip.show)
    //.on('mouseout', tip.hide)
    .on('mouseover', function(d,i){
	var content =
           "<strong>Name:</strong> <span style='color:black'>" + d.name + "</span><br>" +
           "<strong>Description:</strong> <span style='color:black'>" + " " + "</span><br>" + 
           "<strong>e-Mail:</strong> <span style='color:black'>" + d.email + "</span><br>"  +
           "<strong>website:</strong> <span style='color:black'>" + " " + "</span><br>";  
	document.getElementById("info").innerHTML = content;
	//document.getElementById("info").removeClass("hidetip");
    })
    .call(drag);

node.append("text")
    .text(function(d) { return d.name; });

var texts = svg.selectAll("text.label")
    .data(graph.nodes)
    .enter().append("text")
    .attr("class", "label")
    .attr("fill", "black")
    .attr("dx", 15)
    .attr("dy", ".45em")
    .text(function(d) {  return d.name;  });
    
//});

function tick() {
  link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  node.attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; });

  texts.attr("transform", function(d) {
      return "translate(" + d.x + "," + d.y + ")";
   });
}

function dblclick(d) {
  d3.select(this).classed("fixed", d.fixed = false);
}

function dragstart(d) {
  d3.select(this).classed("fixed", d.fixed = true);
}

