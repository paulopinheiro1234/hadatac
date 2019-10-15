let scale=$("#scale");
scale.hide();
var measurements = '@measurements';
var jsonMeasurements = measurements.replace(/&quot;/g,'"');
var obj = JSON.parse(jsonMeasurements);
//alert(JSON.stringify(obj,null,'\t'));
//alert(jsonMeasurements);

//var formatDateIntoYear = d3.timeFormat("%Y");
var formatDateIntoMinute = d3.timeFormat("%H:%M");
var formatDate = d3.timeFormat("%x %X");
//var parseDate = d3.timeParse("%m/%d/%y");

var parseUTCDate = d3.utcParse("%Y-%m-%dT%H:%M:%S.%LZ");
var formatUTCDate = d3.timeFormat("%Y-%m-%d");
var parseDate = d3.timeParse("%Y-%m-%d");
var formatDate2 = d3.timeFormat("%Y-%m-%dT%H:%M:%S.%L");

//var startDate = new Date("2015-11-01"),
//    endDate = new Date("2017-04-01");
var startDate = new Date('@minDate'),
    endDate = new Date('@maxDate');
var startDateTimeDiv = document.getElementById('start_date_time');
startDateTimeDiv.innerHTML = '<br><b>Start Date/Time</b>: ' + startDate + '<br>';
var endDateTimeDiv = document.getElementById('end_date_time');
endDateTimeDiv.innerHTML = '<b>End Date/Time</b>: ' + endDate + '<br>';

//alert(startDate);
//alert(formatDate(startDate));
//alert(endDate);

var margin_hm = {top:0, right:200, bottom:0, left:180},
    width_hm = 960 - margin_hm.left - margin_hm.right,
    height_hm = 300 - margin_hm.top - margin_hm.bottom;

var margin_viz = {top:50, right:100, bottom:0, left:60},
    width_viz = 960 - margin_viz.left - margin_viz.right,
    height_viz = 250 - margin_viz.top - margin_viz.bottom;

var time_viz = d3.select("#vis")
    .append("svg")
    .attr("width", width_viz + margin_viz.left + margin_viz.right)
    .attr("height", height_viz + margin_viz.top + margin_viz.bottom);  

////////// slider //////////

var moving = false;
var currentValue = 0;
var targetValue = width_viz;

var playButton = d3.select("#play-button");
    
var x = d3.scaleTime()
    .domain([startDate, endDate])
    .range([0, targetValue])
    .clamp(true);

var slider = time_viz.append("g")
    .attr("class", "slider")
    .attr("transform", "translate(" + margin_viz.left + "," + height_viz/5 + ")");

slider.append("line")
    .attr("class", "track")
    .attr("x1", x.range()[0])
    .attr("x2", x.range()[1])
  .select(function() { return this.parentNode.appendChild(this.cloneNode(true)); })
    .attr("class", "track-inset")
  .select(function() { return this.parentNode.appendChild(this.cloneNode(true)); })
    .attr("class", "track-overlay")
    .call(d3.drag()
        .on("start.interrupt", function() { slider.interrupt(); })
        .on("start drag", function() {
          currentValue = d3.event.x;
          update(x.invert(currentValue)); 
       })
   );

slider.insert("g", ".track-overlay")
    .attr("class", "ticks")
    .attr("transform", "translate(0," + 18 + ")")
  .selectAll("text")
    .data(x.ticks(10))
    .enter()
    .append("text")
    .attr("x", x)
    .attr("y", 10)
    .attr("text-anchor", "middle")
    .text(function(d) { return formatDateIntoMinute(d); });

var handle = slider.insert("circle", ".track-overlay")
    .attr("class", "handle")
    .attr("r", 9);

var label = slider.append("text")  
    .attr("class", "label")
    .attr("text-anchor", "middle")
    .text(formatDate(startDate))
    .attr("transform", "translate(0," + (-25) + ")");
				 
////////// show GUI //////////

var dataset;

dataset = obj;

showValues(startDate);

for (item in dataset) {
	//prepare(item);
	playButton
	  .on("click", function() {
		  var button = d3.select(this);
		  if (button.text() == "Pause") {
		    moving = false;
		    clearInterval(timer);
	        timer = 0;
		    button.text("Play");
		  } else {
		    moving = true;
		    timer = setInterval(step, 100);
		    button.text("Pause");
		  }
		  console.log("Slider moving: " + moving);
	 });
}

function step() {
   update(x.invert(currentValue));
   currentValue = currentValue + (targetValue/151);
  if (currentValue > targetValue) {
      moving = false;
      currentValue = 0;
      clearInterval(timer);
      timer = 0;
      playButton.text("Play");
      console.log("Slider moving: " + moving);
   }
}

function showValues(h) {
	// format given date parameter h coming from the time slider
	var requestH = formatDate2(h);

	// select from h the corresponding array of values
	var div = document.getElementById('data_values');
	var valueStr = "";
	var valuesArray;
	for (var i = dataset.length - 1; i >= 0; i--) {
		if (dataset[i].date <= requestH) {
			valueStr = dataset[i].value;
			valueArrayStr = valueStr.replace(/;/g,',');
			div.innerHTML = '<b>Data values</b>: ' + valueArrayStr + '<br><br>';
			break;
		}
	}
				
}

function update(h) {
    // update position and text of label according to slider scale
    handle.attr("cx", x(h));
    label
       .attr("x", x(h))
       .text(formatDate(h));
  
    showValues(h);
}
