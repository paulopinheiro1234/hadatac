//Requires the following files to be included in the html
//Openlayers js must be included before this script is called
//"http://www.openlayers.org/api/OpenLayers.js"
//"http://openlayers.org/en/v3.9.0/css/ol.css"
//The function createMap(locations) expects a div in the html with the id map
//<div style="width:95%; height:500px" id="map"></div>


function createMap(locations, imgurl){

    var map;

    //Create the background map
    map = new OpenLayers.Map("map");
    var mapnik = new OpenLayers.Layer.OSM();
    map.addLayer(mapnik);

        
    //Projections for converting between standard lat/lon 
    //and the standard web map projection EPSG:900913
    var epsg = new OpenLayers.Projection("EPSG:4326");
    var osm = new OpenLayers.Projection("EPSG:900913");
    //The center lat and lon will center the view of the map upon loading
    var center_lat;
    var center_lon;
    var zoom;
    var extent;
    var zoom_to_extent;
    var bbox;


    //Generate the bounding box for a list of locations
    if (locations){
        bbox = generate_bbox(locations);
    }

    if (bbox){
    	//alert(bbox);
        if (bbox.length == 4){
            center_lat = bbox[0] + ((bbox[1] - bbox[0]) / 2);
            center_lon = bbox[2] + ((bbox[3] - bbox[2]) / 2);
            extent = new OpenLayers.Bounds(bbox[2],bbox[0],bbox[3],bbox[1]).transform(epsg,osm);
            zoom_to_extent = true;
        }
	else {
	    if (locations.length == 2){
		center_lon = parseFloat(locations[1]);
		center_lat = parseFloat(locations[0]);
	    }
	    zoom = 11;
	} 
    } else {
	alert("Bbox doesn't exist for some reason");
    }

    var centerlonlat = new OpenLayers.LonLat(center_lon, center_lat).transform(
            epsg, // transform from WGS 1984
            osm // to Spherical Mercator
        );

    //Can optionally mark the center of the map
    //markers.addMarker(new OpenLayers.Marker(centerlonlat));

    //These are the variables which will hold the values for a specific instance
    //(I'm using the word "Instance" to refer to a data point, deployment, etc
    var instance_lon;
    var instance_lat;
    var instance_lonlat;

    //var features = [];

    //Add the markers to the map
    if (locations){

        //Create the markers layer
        //This is used to add the icons which mark the locations on the map
        var markers = new OpenLayers.Layer.Markers( "Markers" );
        map.addLayer(markers);
        //var markers = new OpenLayers.Layer.Vector( "Markers" );
    	
    	for (i = 0; i < locations.length - 1; i = i + 2){
            instance_lon = parseFloat(locations[i+1]);
            instance_lat = parseFloat(locations[i]);
            instance_lonlat = new OpenLayers.LonLat(instance_lon, instance_lat).transform(epsg, osm);

            //Make the feature a plain OpenLayers marker
            var marker = new OpenLayers.Marker(instance_lonlat);
            
            /*
            var marker = new OpenLayers.Feature.Vector(
            	new OpenLayers.Geometry.Point(instance_lon, instance_lat).transform(epsg, osm),
            	{description:'This is the value of<br>the description attribute'} ,
            //	{externalGraphic: imgurl, graphicHeight: 25, graphicWidth: 21, graphicXOffset:-12, graphicYOffset:-25  }
            	{
                    fillColor       : '#008040',
                    fillOpacity     : 0.8,                    
                    strokeColor     : "#ee9900",
                    strokeOpacity   : 1,
                    strokeWidth     : 1,
                    pointRadius     : 8,
                    externalGraphic : imgurl, 
                    graphicHeight   : 25, 
                    graphicWidth    : 21, 
                    graphicXOffset  :-12, 
                    graphicYOffset  :-25
                }
            );

            features.push(marker);
            */
            
            marker.events.register('click', marker, function(evt) {
            });      
            markers.addMarker(marker);
            /*
            markers.events.register('click', marker, function(evt) {
            	alert("HERE");
            });
            */      
        }

    	/*
        // Feature's popup support 
        var markers = new OpenLayers.Layer.Vector("Points",{
            eventListeners:{
                'featureselected': function(evt) {
                	var feature = evt.feature;
                	var popup = new OpenLayers.Popup.FramedCloud("popup",
                			OpenLayers.LonLat.fromString(feature.geometry.toShortString()),
                			//instance_lonlat,
                			null,
                			'<div>Hello World! Put your html here</div>',
                			null,
                			false);
                	map.addPopup(popup);
                },
                'featureunselected': function(evt) {
                	var feature = evt.feature;
                	map.removePopup(feature.popup);
                	feature.popup.destroy();
                	feature.popup = null;
                }
            }
        });
        */        	
  	

        /*
        markers.addFeatures(features);

        var selector = new OpenLayers.Control.SelectFeature(markers, {
        	hover: true
        });
        */
    	
        //map.addLayer(markers);
        //map.addControl(selector);
    }

    /*
    selectControl.events.register(
    		'featurehighlighted', 
    		null, 
    		function (evt) {
    		    // Needed only for interaction, not for the display.
    		    var onPopupClose = function (evt) {
    		        // 'this' is the popup.
    		        var feature = this.feature;
    		        if (feature.layer) {
    		            selectControl.unselect(feature);
    		        }  
    		        this.destroy();
    		    }

    		    feature = evt.feature;
    		    popup = new OpenLayers.Popup.FramedCloud("featurePopup",
    		            feature.geometry.getBounds().getCenterLonLat(),
    		            new OpenLayers.Size(100,100),
    		            "test",
    		            null, 
    		            true, 
    		            onPopupClose);
    		    feature.popup = popup;
    		    popup.feature = feature;
    		    map.addPopup(popup, true);
    		}
    ); */
    
    //Set the center of the map so it looks right on loading
    map.setCenter(centerlonlat, zoom);

    if (zoom_to_extent){
        map.zoomToExtent(extent);
    }
}

function generate_bbox(locations){
    var generated_bbox = [];
    var lon;
    var lat;
    var minlon = 100000000.0;
    var minlat = 100000000.0;
    var maxlon = -100000000.0;
    var maxlat = -100000000.0;

    if (locations.length <= 0){
	    return generated_bbox;
    }

    if (locations.length <= 2){
        lon = parseFloat(locations[1]);
        lat = parseFloat(locations[0]);
        minlon = lon - 0.01;
        maxlon = lon + 0.01;
        minlat = lat - 0.01;
        maxlat = lat + 0.01;
    } else {
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
