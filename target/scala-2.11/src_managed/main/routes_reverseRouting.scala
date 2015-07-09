// @SOURCE:/home/tetherless/hadatac-browser/conf/routes
// @HASH:95d974d2a4e305b5b74da2b4b23242f3885cc6ea
// @DATE:Wed Jul 08 15:01:55 EDT 2015

import Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.Router.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._
import _root_.controllers.Assets.Asset
import _root_.play.libs.F

import Router.queryString


// @LINE:40
// @LINE:37
// @LINE:35
// @LINE:34
// @LINE:33
// @LINE:32
// @LINE:31
// @LINE:30
// @LINE:29
// @LINE:28
// @LINE:27
// @LINE:26
// @LINE:25
// @LINE:24
// @LINE:23
// @LINE:22
// @LINE:21
// @LINE:20
// @LINE:19
// @LINE:18
// @LINE:17
// @LINE:16
// @LINE:15
// @LINE:14
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers {

// @LINE:40
class ReverseAssets {


// @LINE:40
def at(file:String): Call = {
   implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
   Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[String]].unbind("file", file))
}
                        

}
                          

// @LINE:15
// @LINE:14
class ReverseMetadata {


// @LINE:15
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata")
}
                        

// @LINE:14
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata")
}
                        

}
                          

// @LINE:25
// @LINE:24
class ReverseDetector {


// @LINE:25
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/detector")
}
                        

// @LINE:24
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/detector")
}
                        

}
                          

// @LINE:13
// @LINE:12
class ReverseHierarchy {


// @LINE:13
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "browser")
}
                        

// @LINE:12
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "browser")
}
                        

}
                          

// @LINE:17
// @LINE:16
class ReversePlatform {


// @LINE:17
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/platform")
}
                        

// @LINE:16
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/platform")
}
                        

}
                          

// @LINE:29
// @LINE:28
class ReverseEntity {


// @LINE:29
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/entity")
}
                        

// @LINE:28
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/entity")
}
                        

}
                          

// @LINE:11
// @LINE:10
class ReverseSpatial {


// @LINE:11
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/spatial")
}
                        

// @LINE:10
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/spatial")
}
                        

}
                          

// @LINE:31
// @LINE:30
class ReverseCharacteristic {


// @LINE:31
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/characteristic")
}
                        

// @LINE:30
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/characteristic")
}
                        

}
                          

// @LINE:37
// @LINE:9
// @LINE:8
class ReverseApplication {


// @LINE:9
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/measurements")
}
                        

// @LINE:8
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/measurements")
}
                        

// @LINE:37
def login(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/login")
}
                        

}
                          

// @LINE:35
// @LINE:34
class ReverseAgent {


// @LINE:35
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/agent")
}
                        

// @LINE:34
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/agent")
}
                        

}
                          

// @LINE:19
// @LINE:18
class ReversePlatformList {


// @LINE:19
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/platformlist")
}
                        

// @LINE:18
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/platformlist")
}
                        

}
                          

// @LINE:23
// @LINE:22
class ReverseInstrumentList {


// @LINE:23
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/instrumentlist")
}
                        

// @LINE:22
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/instrumentlist")
}
                        

}
                          

// @LINE:33
// @LINE:32
class ReverseUnit {


// @LINE:33
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/unit")
}
                        

// @LINE:32
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/unit")
}
                        

}
                          

// @LINE:7
// @LINE:6
class ReversePortal {


// @LINE:7
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/")
}
                        

// @LINE:6
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/")
}
                        

}
                          

// @LINE:21
// @LINE:20
class ReverseInstrument {


// @LINE:21
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/instrument")
}
                        

// @LINE:20
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/instrument")
}
                        

}
                          

// @LINE:27
// @LINE:26
class ReverseDetectorList {


// @LINE:27
def postIndex(): Call = {
   import ReverseRouteContext.empty
   Call("POST", _prefix + { _defaultPrefix } + "hadatac/metadata/detectorlist")
}
                        

// @LINE:26
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "hadatac/metadata/detectorlist")
}
                        

}
                          
}
                  


// @LINE:40
// @LINE:37
// @LINE:35
// @LINE:34
// @LINE:33
// @LINE:32
// @LINE:31
// @LINE:30
// @LINE:29
// @LINE:28
// @LINE:27
// @LINE:26
// @LINE:25
// @LINE:24
// @LINE:23
// @LINE:22
// @LINE:21
// @LINE:20
// @LINE:19
// @LINE:18
// @LINE:17
// @LINE:16
// @LINE:15
// @LINE:14
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.javascript {
import ReverseRouteContext.empty

// @LINE:40
class ReverseAssets {


// @LINE:40
def at : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Assets.at",
   """
      function(file) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("file", file)})
      }
   """
)
                        

}
              

// @LINE:15
// @LINE:14
class ReverseMetadata {


// @LINE:15
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Metadata.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata"})
      }
   """
)
                        

// @LINE:14
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Metadata.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata"})
      }
   """
)
                        

}
              

// @LINE:25
// @LINE:24
class ReverseDetector {


// @LINE:25
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Detector.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/detector"})
      }
   """
)
                        

// @LINE:24
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Detector.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/detector"})
      }
   """
)
                        

}
              

// @LINE:13
// @LINE:12
class ReverseHierarchy {


// @LINE:13
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Hierarchy.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "browser"})
      }
   """
)
                        

// @LINE:12
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Hierarchy.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "browser"})
      }
   """
)
                        

}
              

// @LINE:17
// @LINE:16
class ReversePlatform {


// @LINE:17
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Platform.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/platform"})
      }
   """
)
                        

// @LINE:16
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Platform.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/platform"})
      }
   """
)
                        

}
              

// @LINE:29
// @LINE:28
class ReverseEntity {


// @LINE:29
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Entity.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/entity"})
      }
   """
)
                        

// @LINE:28
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Entity.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/entity"})
      }
   """
)
                        

}
              

// @LINE:11
// @LINE:10
class ReverseSpatial {


// @LINE:11
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Spatial.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/spatial"})
      }
   """
)
                        

// @LINE:10
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Spatial.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/spatial"})
      }
   """
)
                        

}
              

// @LINE:31
// @LINE:30
class ReverseCharacteristic {


// @LINE:31
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Characteristic.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/characteristic"})
      }
   """
)
                        

// @LINE:30
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Characteristic.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/characteristic"})
      }
   """
)
                        

}
              

// @LINE:37
// @LINE:9
// @LINE:8
class ReverseApplication {


// @LINE:9
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Application.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/measurements"})
      }
   """
)
                        

// @LINE:8
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Application.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/measurements"})
      }
   """
)
                        

// @LINE:37
def login : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Application.login",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/login"})
      }
   """
)
                        

}
              

// @LINE:35
// @LINE:34
class ReverseAgent {


// @LINE:35
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Agent.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/agent"})
      }
   """
)
                        

// @LINE:34
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Agent.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/agent"})
      }
   """
)
                        

}
              

// @LINE:19
// @LINE:18
class ReversePlatformList {


// @LINE:19
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.PlatformList.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/platformlist"})
      }
   """
)
                        

// @LINE:18
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.PlatformList.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/platformlist"})
      }
   """
)
                        

}
              

// @LINE:23
// @LINE:22
class ReverseInstrumentList {


// @LINE:23
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.InstrumentList.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/instrumentlist"})
      }
   """
)
                        

// @LINE:22
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.InstrumentList.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/instrumentlist"})
      }
   """
)
                        

}
              

// @LINE:33
// @LINE:32
class ReverseUnit {


// @LINE:33
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Unit.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/unit"})
      }
   """
)
                        

// @LINE:32
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Unit.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/unit"})
      }
   """
)
                        

}
              

// @LINE:7
// @LINE:6
class ReversePortal {


// @LINE:7
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Portal.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/"})
      }
   """
)
                        

// @LINE:6
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Portal.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/"})
      }
   """
)
                        

}
              

// @LINE:21
// @LINE:20
class ReverseInstrument {


// @LINE:21
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Instrument.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/instrument"})
      }
   """
)
                        

// @LINE:20
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Instrument.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/instrument"})
      }
   """
)
                        

}
              

// @LINE:27
// @LINE:26
class ReverseDetectorList {


// @LINE:27
def postIndex : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.DetectorList.postIndex",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/detectorlist"})
      }
   """
)
                        

// @LINE:26
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.DetectorList.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "hadatac/metadata/detectorlist"})
      }
   """
)
                        

}
              
}
        


// @LINE:40
// @LINE:37
// @LINE:35
// @LINE:34
// @LINE:33
// @LINE:32
// @LINE:31
// @LINE:30
// @LINE:29
// @LINE:28
// @LINE:27
// @LINE:26
// @LINE:25
// @LINE:24
// @LINE:23
// @LINE:22
// @LINE:21
// @LINE:20
// @LINE:19
// @LINE:18
// @LINE:17
// @LINE:16
// @LINE:15
// @LINE:14
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.ref {


// @LINE:40
class ReverseAssets {


// @LINE:40
def at(path:String, file:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Assets.at(path, file), HandlerDef(this.getClass.getClassLoader, "", "controllers.Assets", "at", Seq(classOf[String], classOf[String]), "GET", """ Map static resources from the /public folder to the /assets URL path""", _prefix + """assets/$file<.+>""")
)
                      

}
                          

// @LINE:15
// @LINE:14
class ReverseMetadata {


// @LINE:15
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Metadata.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Metadata", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata""")
)
                      

// @LINE:14
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Metadata.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Metadata", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata""")
)
                      

}
                          

// @LINE:25
// @LINE:24
class ReverseDetector {


// @LINE:25
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Detector.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Detector", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/detector""")
)
                      

// @LINE:24
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Detector.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Detector", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/detector""")
)
                      

}
                          

// @LINE:13
// @LINE:12
class ReverseHierarchy {


// @LINE:13
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Hierarchy.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Hierarchy", "postIndex", Seq(), "POST", """""", _prefix + """browser""")
)
                      

// @LINE:12
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Hierarchy.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Hierarchy", "index", Seq(), "GET", """""", _prefix + """browser""")
)
                      

}
                          

// @LINE:17
// @LINE:16
class ReversePlatform {


// @LINE:17
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Platform.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Platform", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/platform""")
)
                      

// @LINE:16
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Platform.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Platform", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/platform""")
)
                      

}
                          

// @LINE:29
// @LINE:28
class ReverseEntity {


// @LINE:29
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Entity.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Entity", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/entity""")
)
                      

// @LINE:28
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Entity.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Entity", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/entity""")
)
                      

}
                          

// @LINE:11
// @LINE:10
class ReverseSpatial {


// @LINE:11
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Spatial.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Spatial", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/spatial""")
)
                      

// @LINE:10
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Spatial.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Spatial", "index", Seq(), "GET", """""", _prefix + """hadatac/spatial""")
)
                      

}
                          

// @LINE:31
// @LINE:30
class ReverseCharacteristic {


// @LINE:31
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Characteristic.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Characteristic", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/characteristic""")
)
                      

// @LINE:30
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Characteristic.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Characteristic", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/characteristic""")
)
                      

}
                          

// @LINE:37
// @LINE:9
// @LINE:8
class ReverseApplication {


// @LINE:9
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Application.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Application", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/measurements""")
)
                      

// @LINE:8
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Application.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Application", "index", Seq(), "GET", """""", _prefix + """hadatac/measurements""")
)
                      

// @LINE:37
def login(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Application.login(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Application", "login", Seq(), "POST", """""", _prefix + """hadatac/login""")
)
                      

}
                          

// @LINE:35
// @LINE:34
class ReverseAgent {


// @LINE:35
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Agent.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Agent", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/agent""")
)
                      

// @LINE:34
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Agent.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Agent", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/agent""")
)
                      

}
                          

// @LINE:19
// @LINE:18
class ReversePlatformList {


// @LINE:19
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.PlatformList.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.PlatformList", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/platformlist""")
)
                      

// @LINE:18
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.PlatformList.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.PlatformList", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/platformlist""")
)
                      

}
                          

// @LINE:23
// @LINE:22
class ReverseInstrumentList {


// @LINE:23
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.InstrumentList.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.InstrumentList", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/instrumentlist""")
)
                      

// @LINE:22
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.InstrumentList.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.InstrumentList", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/instrumentlist""")
)
                      

}
                          

// @LINE:33
// @LINE:32
class ReverseUnit {


// @LINE:33
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Unit.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Unit", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/unit""")
)
                      

// @LINE:32
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Unit.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Unit", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/unit""")
)
                      

}
                          

// @LINE:7
// @LINE:6
class ReversePortal {


// @LINE:7
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Portal.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Portal", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/""")
)
                      

// @LINE:6
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Portal.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Portal", "index", Seq(), "GET", """ Home page""", _prefix + """hadatac/""")
)
                      

}
                          

// @LINE:21
// @LINE:20
class ReverseInstrument {


// @LINE:21
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Instrument.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Instrument", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/instrument""")
)
                      

// @LINE:20
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Instrument.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Instrument", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/instrument""")
)
                      

}
                          

// @LINE:27
// @LINE:26
class ReverseDetectorList {


// @LINE:27
def postIndex(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.DetectorList.postIndex(), HandlerDef(this.getClass.getClassLoader, "", "controllers.DetectorList", "postIndex", Seq(), "POST", """""", _prefix + """hadatac/metadata/detectorlist""")
)
                      

// @LINE:26
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.DetectorList.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.DetectorList", "index", Seq(), "GET", """""", _prefix + """hadatac/metadata/detectorlist""")
)
                      

}
                          
}
        
    