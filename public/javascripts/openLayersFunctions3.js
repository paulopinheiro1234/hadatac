//Requires the following files to be included in the html
//ol js must be included before this script is called
//"http://www.ol.org/api/ol.js"
//"http://ol.org/en/v3.9.0/css/ol.css"
//The function createMap(locations) expects a div in the html with the id map
//<div style="width:95%; height:500px" id="map"></div>


function createMap(dir, filename, da_uri, browser_url, locations, names, uris) {

	//alert(browser_url);
	//alert(locations);
	//alert(names);
	//alert(uris);
	var center_lat;
    var center_lon;
    var zoomValue;
    var bbox;

    //Generate the bounding box for a list of locations
    if (locations){
        bbox = generate_bbox(locations);
    }

    if (bbox){
        if (bbox.length == 4){
            center_lat = bbox[0] + ((bbox[1] - bbox[0]) / 2);
            center_lon = bbox[2] + ((bbox[3] - bbox[2]) / 2);
            zoom_to_extent = true;
        } else {
        	if (locations.length == 2){
        		center_lon = parseFloat(locations[1]);
        		center_lat = parseFloat(locations[0]);
        	}
        	zoomValue = 11;
        } 
    } else {
    	alert("Bbox doesn't exist for some reason");
    }

    var map = new ol.Map({
      layers: [
        new ol.layer.Tile({
          source: new ol.source.OSM()
        })
      ],
      target: 'map',
      view: new ol.View({
        projection: 'EPSG:3857',
        center: ol.proj.fromLonLat([center_lon, center_lat], 'EPSG:3857'),
        zoom: 11,
        minZoom: 6
      })
    });
    
    //Add the markers to the map
    var iconFeatures=[];
    if (locations){
    	var j = 0;
    	for (i = 0; i < locations.length - 1; i = i + 2){
            instance_lon = parseFloat(locations[i+1]);
            instance_lat = parseFloat(locations[i]);

            var feature = new ol.Feature({
                geometry: new ol.geom.Point(ol.proj.fromLonLat([instance_lon, instance_lat])),
                description : names[j],
            	uri : uris[j]
            });
            iconFeatures.push(feature);
            j++;
    	}
    }
    var vectorSource = new ol.source.Vector({
    	  features: iconFeatures //add an array of features
    	});
    var iconStyle = new ol.style.Style({
    	  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    	    anchor: [0.5, 46],
    	    anchorXUnits: 'fraction',
    	    anchorYUnits: 'pixels',
    	    opacity: 0.75,
            crossOrigin: 'anonymous',
    	    src: '../../hadatac/media/marker.png'
    	  }))
    	});
    var vectorLayer = new ol.layer.Vector({
    	  source: vectorSource,
    	  style: iconStyle
    	});
    
    map.addLayer(vectorLayer);

    var extent = ol.proj.transformExtent([bbox[2], bbox[0], bbox[3], bbox[1]],
            'EPSG:4326', 'EPSG:3857');
    map.getView().fit(extent, map.getSize()); 

    var info = $('#info');
    info.tooltip({
        animation: false,
        trigger: 'manual'
    });

    var displayFeatureInfo = function(pixel) {
        info.css({
            left: pixel[0] + 'px',
            top: (pixel[1] - 15) + 'px'
        });
        var feature = map.forEachFeatureAtPixel(pixel, function(feature, layer) {
            return feature;
        });
        if (feature) {
            info.tooltip('hide')
                .attr('data-original-title', feature.get('description'))
                .tooltip('fixTitle')
                .tooltip('show');
        } else {
            info.tooltip('hide');
        }
    };

    var displayFeatureInfo2 = function(pixel) {
        info.css({
            left: pixel[0] + 'px',
            top: (pixel[1] - 15) + 'px'
        });
        var feature = map.forEachFeatureAtPixel(pixel, function(feature, layer) {
            return feature;
        });
        if (feature) {
        	//alert(browser_url.replace("XXXX",feature.get('uri')).replace(/amp;/g,"").replace(/#/g,"%23"));
        	window.location.href = browser_url.replace("XXXX",feature.get('uri')).replace(/amp;/g,"").replace(/#/g,"%23");
        } 
    };

    map.on('pointermove', function(evt) {
        if (evt.dragging) {
            info.tooltip('hide');
            return;
        }
        displayFeatureInfo(map.getEventPixel(evt.originalEvent));
    });
    map.on('click', function(evt) {
        if (evt.dragging) {
            info.tooltip('hide');
            return;
        }
        displayFeatureInfo2(map.getEventPixel(evt.originalEvent));
    });
          
}

function generate_bbox(locations){
    var generated_bbox = [];
    var lon;
    var lat;
    var minlon;
    var minlat;
    var maxlon;
    var maxlat;

    if (locations.length <= 0){
	return generated_bbox;
    }

    if (locations.length <= 2) {
        lon = parseFloat(locations[1]);
        lat = parseFloat(locations[0]);
        minlon = lon - 0.0008;
        maxlon = lon + 0.0008;
        minlat = lat - 0.0008;
        maxlat = lat + 0.0008;
    } else {
        minlon = 100000000.0;
        minlat = 100000000.0;
        maxlon = -100000000.0;
        maxlat = -100000000.0;
    	for (i = 0; i < locations.length - 1; i = i + 2){
	        lon = parseFloat(locations[i+1]);
	        lat = parseFloat(locations[i]);
	        if (lon < minlon){
	            minlon = lon;
	        }
	        if (lon > maxlon){
	            maxlon = lon; 
	        }
	        if (lat < minlat){
	            minlat = lat;
	        }
	        if (lat > maxlat){
	            maxlat = lat;
	        }
    	}
    }
    	
    //Order is min lat, max lat, min lon, max lon
    generated_bbox.push(minlat);
    generated_bbox.push(maxlat);
    generated_bbox.push(minlon);
    generated_bbox.push(maxlon);
    return generated_bbox;
}
