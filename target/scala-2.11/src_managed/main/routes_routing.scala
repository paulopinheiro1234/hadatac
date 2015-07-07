// @SOURCE:/Users/paulopinheiro/Documents/workspace/metadata-browser/conf/routes
// @HASH:95d974d2a4e305b5b74da2b4b23242f3885cc6ea
// @DATE:Tue Jul 07 15:05:19 EDT 2015


import scala.language.reflectiveCalls
import play.core._
import play.core.Router._
import play.core.Router.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._
import _root_.controllers.Assets.Asset
import _root_.play.libs.F

import Router.queryString

object Routes extends Router.Routes {

import ReverseRouteContext.empty

private var _prefix = "/"

def setPrefix(prefix: String): Unit = {
  _prefix = prefix
  List[(String,Routes)]().foreach {
    case (p, router) => router.setPrefix(prefix + (if(prefix.endsWith("/")) "" else "/") + p)
  }
}

def prefix = _prefix

lazy val defaultPrefix = { if(Routes.prefix.endsWith("/")) "" else "/" }


// @LINE:6
private[this] lazy val controllers_Portal_index0_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/"))))
private[this] lazy val controllers_Portal_index0_invoker = createInvoker(
controllers.Portal.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Portal", "index", Nil,"GET", """ Home page""", Routes.prefix + """hadatac/"""))
        

// @LINE:7
private[this] lazy val controllers_Portal_postIndex1_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/"))))
private[this] lazy val controllers_Portal_postIndex1_invoker = createInvoker(
controllers.Portal.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Portal", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/"""))
        

// @LINE:8
private[this] lazy val controllers_Application_index2_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/measurements"))))
private[this] lazy val controllers_Application_index2_invoker = createInvoker(
controllers.Application.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Application", "index", Nil,"GET", """""", Routes.prefix + """hadatac/measurements"""))
        

// @LINE:9
private[this] lazy val controllers_Application_postIndex3_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/measurements"))))
private[this] lazy val controllers_Application_postIndex3_invoker = createInvoker(
controllers.Application.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Application", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/measurements"""))
        

// @LINE:10
private[this] lazy val controllers_Spatial_index4_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/spatial"))))
private[this] lazy val controllers_Spatial_index4_invoker = createInvoker(
controllers.Spatial.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Spatial", "index", Nil,"GET", """""", Routes.prefix + """hadatac/spatial"""))
        

// @LINE:11
private[this] lazy val controllers_Spatial_postIndex5_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/spatial"))))
private[this] lazy val controllers_Spatial_postIndex5_invoker = createInvoker(
controllers.Spatial.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Spatial", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/spatial"""))
        

// @LINE:12
private[this] lazy val controllers_Hierarchy_index6_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("browser"))))
private[this] lazy val controllers_Hierarchy_index6_invoker = createInvoker(
controllers.Hierarchy.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Hierarchy", "index", Nil,"GET", """""", Routes.prefix + """browser"""))
        

// @LINE:13
private[this] lazy val controllers_Hierarchy_postIndex7_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("browser"))))
private[this] lazy val controllers_Hierarchy_postIndex7_invoker = createInvoker(
controllers.Hierarchy.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Hierarchy", "postIndex", Nil,"POST", """""", Routes.prefix + """browser"""))
        

// @LINE:14
private[this] lazy val controllers_Metadata_index8_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata"))))
private[this] lazy val controllers_Metadata_index8_invoker = createInvoker(
controllers.Metadata.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Metadata", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata"""))
        

// @LINE:15
private[this] lazy val controllers_Metadata_postIndex9_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata"))))
private[this] lazy val controllers_Metadata_postIndex9_invoker = createInvoker(
controllers.Metadata.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Metadata", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata"""))
        

// @LINE:16
private[this] lazy val controllers_Platform_index10_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/platform"))))
private[this] lazy val controllers_Platform_index10_invoker = createInvoker(
controllers.Platform.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Platform", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/platform"""))
        

// @LINE:17
private[this] lazy val controllers_Platform_postIndex11_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/platform"))))
private[this] lazy val controllers_Platform_postIndex11_invoker = createInvoker(
controllers.Platform.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Platform", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/platform"""))
        

// @LINE:18
private[this] lazy val controllers_PlatformList_index12_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/platformlist"))))
private[this] lazy val controllers_PlatformList_index12_invoker = createInvoker(
controllers.PlatformList.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.PlatformList", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/platformlist"""))
        

// @LINE:19
private[this] lazy val controllers_PlatformList_postIndex13_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/platformlist"))))
private[this] lazy val controllers_PlatformList_postIndex13_invoker = createInvoker(
controllers.PlatformList.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.PlatformList", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/platformlist"""))
        

// @LINE:20
private[this] lazy val controllers_Instrument_index14_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/instrument"))))
private[this] lazy val controllers_Instrument_index14_invoker = createInvoker(
controllers.Instrument.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Instrument", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/instrument"""))
        

// @LINE:21
private[this] lazy val controllers_Instrument_postIndex15_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/instrument"))))
private[this] lazy val controllers_Instrument_postIndex15_invoker = createInvoker(
controllers.Instrument.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Instrument", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/instrument"""))
        

// @LINE:22
private[this] lazy val controllers_InstrumentList_index16_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/instrumentlist"))))
private[this] lazy val controllers_InstrumentList_index16_invoker = createInvoker(
controllers.InstrumentList.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.InstrumentList", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/instrumentlist"""))
        

// @LINE:23
private[this] lazy val controllers_InstrumentList_postIndex17_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/instrumentlist"))))
private[this] lazy val controllers_InstrumentList_postIndex17_invoker = createInvoker(
controllers.InstrumentList.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.InstrumentList", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/instrumentlist"""))
        

// @LINE:24
private[this] lazy val controllers_Detector_index18_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/detector"))))
private[this] lazy val controllers_Detector_index18_invoker = createInvoker(
controllers.Detector.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Detector", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/detector"""))
        

// @LINE:25
private[this] lazy val controllers_Detector_postIndex19_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/detector"))))
private[this] lazy val controllers_Detector_postIndex19_invoker = createInvoker(
controllers.Detector.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Detector", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/detector"""))
        

// @LINE:26
private[this] lazy val controllers_DetectorList_index20_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/detectorlist"))))
private[this] lazy val controllers_DetectorList_index20_invoker = createInvoker(
controllers.DetectorList.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.DetectorList", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/detectorlist"""))
        

// @LINE:27
private[this] lazy val controllers_DetectorList_postIndex21_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/detectorlist"))))
private[this] lazy val controllers_DetectorList_postIndex21_invoker = createInvoker(
controllers.DetectorList.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.DetectorList", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/detectorlist"""))
        

// @LINE:28
private[this] lazy val controllers_Entity_index22_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/entity"))))
private[this] lazy val controllers_Entity_index22_invoker = createInvoker(
controllers.Entity.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Entity", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/entity"""))
        

// @LINE:29
private[this] lazy val controllers_Entity_postIndex23_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/entity"))))
private[this] lazy val controllers_Entity_postIndex23_invoker = createInvoker(
controllers.Entity.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Entity", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/entity"""))
        

// @LINE:30
private[this] lazy val controllers_Characteristic_index24_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/characteristic"))))
private[this] lazy val controllers_Characteristic_index24_invoker = createInvoker(
controllers.Characteristic.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Characteristic", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/characteristic"""))
        

// @LINE:31
private[this] lazy val controllers_Characteristic_postIndex25_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/characteristic"))))
private[this] lazy val controllers_Characteristic_postIndex25_invoker = createInvoker(
controllers.Characteristic.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Characteristic", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/characteristic"""))
        

// @LINE:32
private[this] lazy val controllers_Unit_index26_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/unit"))))
private[this] lazy val controllers_Unit_index26_invoker = createInvoker(
controllers.Unit.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Unit", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/unit"""))
        

// @LINE:33
private[this] lazy val controllers_Unit_postIndex27_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/unit"))))
private[this] lazy val controllers_Unit_postIndex27_invoker = createInvoker(
controllers.Unit.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Unit", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/unit"""))
        

// @LINE:34
private[this] lazy val controllers_Agent_index28_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/agent"))))
private[this] lazy val controllers_Agent_index28_invoker = createInvoker(
controllers.Agent.index(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Agent", "index", Nil,"GET", """""", Routes.prefix + """hadatac/metadata/agent"""))
        

// @LINE:35
private[this] lazy val controllers_Agent_postIndex29_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/metadata/agent"))))
private[this] lazy val controllers_Agent_postIndex29_invoker = createInvoker(
controllers.Agent.postIndex(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Agent", "postIndex", Nil,"POST", """""", Routes.prefix + """hadatac/metadata/agent"""))
        

// @LINE:37
private[this] lazy val controllers_Application_login30_route = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("hadatac/login"))))
private[this] lazy val controllers_Application_login30_invoker = createInvoker(
controllers.Application.login(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Application", "login", Nil,"POST", """""", Routes.prefix + """hadatac/login"""))
        

// @LINE:40
private[this] lazy val controllers_Assets_at31_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("assets/"),DynamicPart("file", """.+""",false))))
private[this] lazy val controllers_Assets_at31_invoker = createInvoker(
controllers.Assets.at(fakeValue[String], fakeValue[String]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Assets", "at", Seq(classOf[String], classOf[String]),"GET", """ Map static resources from the /public folder to the /assets URL path""", Routes.prefix + """assets/$file<.+>"""))
        
def documentation = List(("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/""","""controllers.Portal.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/""","""controllers.Portal.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/measurements""","""controllers.Application.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/measurements""","""controllers.Application.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/spatial""","""controllers.Spatial.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/spatial""","""controllers.Spatial.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """browser""","""controllers.Hierarchy.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """browser""","""controllers.Hierarchy.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata""","""controllers.Metadata.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata""","""controllers.Metadata.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/platform""","""controllers.Platform.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/platform""","""controllers.Platform.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/platformlist""","""controllers.PlatformList.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/platformlist""","""controllers.PlatformList.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/instrument""","""controllers.Instrument.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/instrument""","""controllers.Instrument.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/instrumentlist""","""controllers.InstrumentList.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/instrumentlist""","""controllers.InstrumentList.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/detector""","""controllers.Detector.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/detector""","""controllers.Detector.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/detectorlist""","""controllers.DetectorList.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/detectorlist""","""controllers.DetectorList.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/entity""","""controllers.Entity.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/entity""","""controllers.Entity.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/characteristic""","""controllers.Characteristic.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/characteristic""","""controllers.Characteristic.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/unit""","""controllers.Unit.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/unit""","""controllers.Unit.postIndex()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/agent""","""controllers.Agent.index()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/metadata/agent""","""controllers.Agent.postIndex()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """hadatac/login""","""controllers.Application.login()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """assets/$file<.+>""","""controllers.Assets.at(path:String = "/public", file:String)""")).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
  case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
  case l => s ++ l.asInstanceOf[List[(String,String,String)]]
}}
      

def routes:PartialFunction[RequestHeader,Handler] = {

// @LINE:6
case controllers_Portal_index0_route(params) => {
   call { 
        controllers_Portal_index0_invoker.call(controllers.Portal.index())
   }
}
        

// @LINE:7
case controllers_Portal_postIndex1_route(params) => {
   call { 
        controllers_Portal_postIndex1_invoker.call(controllers.Portal.postIndex())
   }
}
        

// @LINE:8
case controllers_Application_index2_route(params) => {
   call { 
        controllers_Application_index2_invoker.call(controllers.Application.index())
   }
}
        

// @LINE:9
case controllers_Application_postIndex3_route(params) => {
   call { 
        controllers_Application_postIndex3_invoker.call(controllers.Application.postIndex())
   }
}
        

// @LINE:10
case controllers_Spatial_index4_route(params) => {
   call { 
        controllers_Spatial_index4_invoker.call(controllers.Spatial.index())
   }
}
        

// @LINE:11
case controllers_Spatial_postIndex5_route(params) => {
   call { 
        controllers_Spatial_postIndex5_invoker.call(controllers.Spatial.postIndex())
   }
}
        

// @LINE:12
case controllers_Hierarchy_index6_route(params) => {
   call { 
        controllers_Hierarchy_index6_invoker.call(controllers.Hierarchy.index())
   }
}
        

// @LINE:13
case controllers_Hierarchy_postIndex7_route(params) => {
   call { 
        controllers_Hierarchy_postIndex7_invoker.call(controllers.Hierarchy.postIndex())
   }
}
        

// @LINE:14
case controllers_Metadata_index8_route(params) => {
   call { 
        controllers_Metadata_index8_invoker.call(controllers.Metadata.index())
   }
}
        

// @LINE:15
case controllers_Metadata_postIndex9_route(params) => {
   call { 
        controllers_Metadata_postIndex9_invoker.call(controllers.Metadata.postIndex())
   }
}
        

// @LINE:16
case controllers_Platform_index10_route(params) => {
   call { 
        controllers_Platform_index10_invoker.call(controllers.Platform.index())
   }
}
        

// @LINE:17
case controllers_Platform_postIndex11_route(params) => {
   call { 
        controllers_Platform_postIndex11_invoker.call(controllers.Platform.postIndex())
   }
}
        

// @LINE:18
case controllers_PlatformList_index12_route(params) => {
   call { 
        controllers_PlatformList_index12_invoker.call(controllers.PlatformList.index())
   }
}
        

// @LINE:19
case controllers_PlatformList_postIndex13_route(params) => {
   call { 
        controllers_PlatformList_postIndex13_invoker.call(controllers.PlatformList.postIndex())
   }
}
        

// @LINE:20
case controllers_Instrument_index14_route(params) => {
   call { 
        controllers_Instrument_index14_invoker.call(controllers.Instrument.index())
   }
}
        

// @LINE:21
case controllers_Instrument_postIndex15_route(params) => {
   call { 
        controllers_Instrument_postIndex15_invoker.call(controllers.Instrument.postIndex())
   }
}
        

// @LINE:22
case controllers_InstrumentList_index16_route(params) => {
   call { 
        controllers_InstrumentList_index16_invoker.call(controllers.InstrumentList.index())
   }
}
        

// @LINE:23
case controllers_InstrumentList_postIndex17_route(params) => {
   call { 
        controllers_InstrumentList_postIndex17_invoker.call(controllers.InstrumentList.postIndex())
   }
}
        

// @LINE:24
case controllers_Detector_index18_route(params) => {
   call { 
        controllers_Detector_index18_invoker.call(controllers.Detector.index())
   }
}
        

// @LINE:25
case controllers_Detector_postIndex19_route(params) => {
   call { 
        controllers_Detector_postIndex19_invoker.call(controllers.Detector.postIndex())
   }
}
        

// @LINE:26
case controllers_DetectorList_index20_route(params) => {
   call { 
        controllers_DetectorList_index20_invoker.call(controllers.DetectorList.index())
   }
}
        

// @LINE:27
case controllers_DetectorList_postIndex21_route(params) => {
   call { 
        controllers_DetectorList_postIndex21_invoker.call(controllers.DetectorList.postIndex())
   }
}
        

// @LINE:28
case controllers_Entity_index22_route(params) => {
   call { 
        controllers_Entity_index22_invoker.call(controllers.Entity.index())
   }
}
        

// @LINE:29
case controllers_Entity_postIndex23_route(params) => {
   call { 
        controllers_Entity_postIndex23_invoker.call(controllers.Entity.postIndex())
   }
}
        

// @LINE:30
case controllers_Characteristic_index24_route(params) => {
   call { 
        controllers_Characteristic_index24_invoker.call(controllers.Characteristic.index())
   }
}
        

// @LINE:31
case controllers_Characteristic_postIndex25_route(params) => {
   call { 
        controllers_Characteristic_postIndex25_invoker.call(controllers.Characteristic.postIndex())
   }
}
        

// @LINE:32
case controllers_Unit_index26_route(params) => {
   call { 
        controllers_Unit_index26_invoker.call(controllers.Unit.index())
   }
}
        

// @LINE:33
case controllers_Unit_postIndex27_route(params) => {
   call { 
        controllers_Unit_postIndex27_invoker.call(controllers.Unit.postIndex())
   }
}
        

// @LINE:34
case controllers_Agent_index28_route(params) => {
   call { 
        controllers_Agent_index28_invoker.call(controllers.Agent.index())
   }
}
        

// @LINE:35
case controllers_Agent_postIndex29_route(params) => {
   call { 
        controllers_Agent_postIndex29_invoker.call(controllers.Agent.postIndex())
   }
}
        

// @LINE:37
case controllers_Application_login30_route(params) => {
   call { 
        controllers_Application_login30_invoker.call(controllers.Application.login())
   }
}
        

// @LINE:40
case controllers_Assets_at31_route(params) => {
   call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at31_invoker.call(controllers.Assets.at(path, file))
   }
}
        
}

}
     