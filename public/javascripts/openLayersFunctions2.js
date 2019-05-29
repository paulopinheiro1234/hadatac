//Requires the following files to be included in the html
//ol js must be included before this script is called
//"http://www.ol.org/api/ol.js"
//"http://ol.org/en/v3.9.0/css/ol.css"
//The function createMap(locations) expects a div in the html with the id map
//<div style="width:95%; height:500px" id="map"></div>


function createMap(locations){

    var map;

    //Projections for converting between standard lat/lon 
    //and the standard web map projection EPSG:900913
    //var epsg = new ol.Projection("EPSG:4326");
    //var osm = new ol.Projection("EPSG:900913");
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
    	alert(bbox);
        if (bbox.length == 4){
            center_lat = bbox[0] + ((bbox[1] - bbox[0]) / 2);
            center_lon = bbox[2] + ((bbox[3] - bbox[2]) / 2);
            //extent = new ol.Bounds(bbox[2],bbox[0],bbox[3],bbox[1]).transform(epsg,osm);
            var extent = ol.proj.transformExtent(
            	    [bbox[2], bbox[0], bbox[3], bbox[1]],
            	    "EPSG:4326", "EPSG:3857"
            	);
            //var textent = ol.proj.transformExtent(bbox[2], bbox[0], bbox[3], bbox[1], 'EPSG:4326', 'EPSG:3857');
            zoom_to_extent = true;
        } else {
        	if (locations.length == 2)	{
        		center_lon = parseFloat(locations[1]);
        		center_lat = parseFloat(locations[0]);
        	}
        	zoom = 11;
        }	 
    } else {
	   alert("Bbox doesn't exist for some reason");
    }

    //var centerlonlat = new ol.LonLat(center_lon, center_lat).transform(
    //        epsg, // transform from WGS 1984
    //        osm // to Spherical Mercator
    //    );

    alert(center_lon + "  " + center_lat);
    
    var map = new ol.Map({
        layers: [
          new ol.layer.Tile({
            source: new ol.source.OSM()
          })
        ],
        target: 'map',
        view: new ol.View({
          projection: 'Indiana-East',
          //center: ol.proj.fromLonLat([0, 0], 'Indiana-East'),
          zoom: 7,
          extent: ol.proj.transformExtent([-10,-10.10,10],
            'EPSG:4326', 'Indiana-East'),
          minZoom: 6
        })
      });
    
    
    //Create the background map
    //map = new ol.Map("map");
    //var mapnik = new ol.Layer.osm();
    //map.addLayer(mapnik);
    /*
    var map = new ol.Map({
        layers: [
            new ol.layer.Tile({
                source: new ol.source.OSM({
                    url: 'https://tile.openstreetmap.be/osmbe/{z}/{x}/{y}.png',
                    attributions: [ ol.source.OSM.ATTRIBUTION, 'Tiles courtesy of <a href="https://geo6.be/">GEO-6</a>' ],
                    maxZoom: 18
                })
            })
        ],
    	target: 'map',
    	view: new ol.View({
    		projection: 'EPSG:4326',
    		center: [center_lon, center_lat],
    		zoom: zoom
    	})
    });
    alert('done');
    
    if (zoom_to_extent) {
    	map.getView().fit( extent, map.getSize() );
    }
    */

    
    /*
    //Can optionally mark the center of the map
    //markers.addMarker(new ol.Marker(centerlonlat));

    //These are the variables which will hold the values for a specific instance
    //(I'm using the word "Instance" to refer to a data point, deployment, etc
    var instance_lon;
    var instance_lat;
    var instance_lonlat;

    var iconFeatures=[];
    
    //Add the markers to the map
    if (locations){

        //Create the markers layer
        //This is used to add the icons which mark the locations on the map
        //var markers = new ol.Layer.Markers( "Markers" );
        //map.addLayer(markers);
        //var markers = new ol.Layer.Vector( "Markers" );
    	
    	for (i = 0; i < locations.length - 1; i = i + 2){
            instance_lon = parseFloat(locations[i+1]);
            instance_lat = parseFloat(locations[i]);
            //instance_lonlat = new ol.LonLat(instance_lon, instance_lat).transform(epsg, osm);

            var feature = new ol.Feature({
                geometry: new ol.geom.Point(ol.proj.fromLonLat([instance_lon, instance_lat])),
                description : 'test'
              });
            
            //var feature = new ol.Feature.Vector(
            //        new ol.Geometry.Point( instance_lon, instance_lat ).transform(epsg, osm),
            //        {description:'test'} ,
            //        {externalGraphic: '../../hadatac/media/marker.png', graphicHeight: 25, graphicWidth: 21, graphicXOffset:-12, graphicYOffset:-25  } 
            //    );    
            //vectorLayer.addFeatures(feature);
            
            iconFeatures.push(feature);

    	}

    }

    //var vectorLayer = new ol.Layer.Vector("Overlay");
    var vectorSource = new ol.source.Vector({
    	  features: iconFeatures //add an array of features
    	});
    */
    
    //var iconStyle = new ol.style.Style({
    //	  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    //	    anchor: [0.5, 46],
   // 	    anchorXUnits: 'fraction',
   // 	    anchorYUnits: 'pixels',
   // 	    opacity: 0.75,
    //        crossOrigin: 'anonymous',
   // 	    src: '../../hadatac/media/marker.png'
    //	  }))
    //	});


    /*
    var vectorLayer = new ol.layer.Vector({
    	  source: vectorSource,
    	  style: iconStyle
    	});
    
    map.addLayer(vectorLayer);
    */
    
   /*
    //Add a selector control to the vectorLayer with popup functions
    var controls = {
      selector: new ol.Control.SelectFeature(vectorLayer, { onSelect: createPopup, onUnselect: destroyPopup })
    };

    function createPopup(feature) {
      feature.popup = new ol.Popup.FramedCloud("pop",
          feature.geometry.getBounds().getCenterLonLat(),
          null,
          '<div class="markerContent">'+feature.attributes.description+'</div>',
          null,
          true,
          function() { controls['selector'].unselectAll(); }
      );
      //feature.popup.closeOnMove = true;
      map.addPopup(feature.popup);
    }

    function destroyPopup(feature) {
      feature.popup.destroy();
      feature.popup = null;
    }

    map.addControl(controls['selector']);
    controls['selector'].activate();
      
    //Set the center of the map so it looks right on loading
    map.setCenter(centerlonlat, zoom);

    if (zoom_to_extent){
        map.zoomToExtent(extent);
    }
    
    */
}

function generate_bbox(locations){
    var generated_bbox = [];
    var lon;
    var lat;
    var minlon = 100000000.0;
    var minlat = 100000000.0;
    var maxlon = -100000000.0;
    var maxlat = -100000000.0;

    if (locations.length <= 2){
	return generated_bbox;
    }

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

    //Order is min lat, max lat, min lon, max lon
    generated_bbox.push(minlat);
    generated_bbox.push(maxlat);
    generated_bbox.push(minlon);
    generated_bbox.push(maxlon);
    return generated_bbox;
}
