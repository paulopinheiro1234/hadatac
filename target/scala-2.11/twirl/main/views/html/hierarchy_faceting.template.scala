
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._

import play.api.templates.PlayMagic._
import models._
import controllers._
import java.lang._
import java.util._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import play.api.i18n._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.data._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._
import views.html._

/**/
object hierarchy_faceting extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template3[Map[String, models.SparqlQueryResults],Map[String, String],String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/( documentMap : Map[String, models.SparqlQueryResults],
   documentHierarchy: Map[String, String],
   final_query : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._
import play.api.libs.json._

Seq[Any](format.raw/*3.25*/("""

"""),format.raw/*7.1*/("""
"""),_display_(/*8.2*/main("Hierarchy Faceted Search")/*8.34*/ {_display_(Seq[Any](format.raw/*8.36*/("""
    """),format.raw/*9.5*/("""<div class="container-fluid">
      
      <div class="row">
        <div class="col-sm-9 col-md-10 main">
          <h3 class="page-header">Metadata Browser</h3>
		  <ul class="nav nav-tabs">
		    """),_display_(/*15.8*/for((thingType, query_results) <- documentMap) yield /*15.54*/ {_display_(Seq[Any](format.raw/*15.56*/("""
    		    """),format.raw/*16.11*/("""<li><a data-toggle="tab" href="#"""),_display_(/*16.44*/thingType),format.raw/*16.53*/("""">"""),_display_(/*16.56*/thingType),format.raw/*16.65*/("""</a></li>
		    """)))}),format.raw/*17.8*/("""
		    """),_display_(/*18.8*/for((thingType, hierarchy_results) <- documentHierarchy) yield /*18.64*/ {_display_(Seq[Any](format.raw/*18.66*/("""
    		    """),format.raw/*19.11*/("""<li><a data-toggle="tab" href="#"""),_display_(/*19.44*/thingType),format.raw/*19.53*/("""">"""),_display_(/*19.56*/thingType),format.raw/*19.65*/("""</a></li>
		    """)))}),format.raw/*20.8*/("""
		  """),format.raw/*21.5*/("""</ul>
          <div class = "tab-content">
            """),_display_(/*23.14*/for((thingType, query_results) <- documentMap) yield /*23.60*/ {_display_(Seq[Any](format.raw/*23.62*/("""
      			"""),format.raw/*24.10*/("""<div id=""""),_display_(/*24.20*/thingType),format.raw/*24.29*/("""" class="tab-pane fade in">
      				"""),_display_(/*25.12*/metadata_table(query_results)),format.raw/*25.41*/("""
      			"""),format.raw/*26.10*/("""</div>
      		""")))}),format.raw/*27.10*/("""
            """),_display_(/*28.14*/for((thingType, hierarchy_results) <- documentHierarchy) yield /*28.70*/ {_display_(Seq[Any](format.raw/*28.72*/("""
      			"""),format.raw/*29.10*/("""<div id=""""),_display_(/*29.20*/thingType),format.raw/*29.29*/("""" class="tab-pane fade in">
  					"""),_display_(/*30.9*/metadata_hierarchy(hierarchy_results)),format.raw/*30.46*/("""
      			"""),format.raw/*31.10*/("""</div>
      		""")))}),format.raw/*32.10*/("""
          """),format.raw/*33.11*/("""</div>
        </div>
      </div>
    </div>

""")))}),format.raw/*38.2*/("""
"""))}
  }

  def render(documentMap:Map[String, models.SparqlQueryResults],documentHierarchy:Map[String, String],final_query:String): play.twirl.api.HtmlFormat.Appendable = apply(documentMap,documentHierarchy,final_query)

  def f:((Map[String, models.SparqlQueryResults],Map[String, String],String) => play.twirl.api.HtmlFormat.Appendable) = (documentMap,documentHierarchy,final_query) => apply(documentMap,documentHierarchy,final_query)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:01:57 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/hierarchy_faceting.scala.html
                  HASH: 0e5fcb7ad237d90c04015d2a6a4d4b62dc79a319
                  MATRIX: 795->1|1049->124|1077->172|1104->174|1144->206|1183->208|1214->213|1440->413|1502->459|1542->461|1581->472|1641->505|1671->514|1701->517|1731->526|1778->543|1812->551|1884->607|1924->609|1963->620|2023->653|2053->662|2083->665|2113->674|2160->691|2192->696|2276->753|2338->799|2378->801|2416->811|2453->821|2483->830|2549->869|2599->898|2637->908|2684->924|2725->938|2797->994|2837->996|2875->1006|2912->1016|2942->1025|3004->1061|3062->1098|3100->1108|3147->1124|3186->1135|3264->1183
                  LINES: 26->1|32->3|34->7|35->8|35->8|35->8|36->9|42->15|42->15|42->15|43->16|43->16|43->16|43->16|43->16|44->17|45->18|45->18|45->18|46->19|46->19|46->19|46->19|46->19|47->20|48->21|50->23|50->23|50->23|51->24|51->24|51->24|52->25|52->25|53->26|54->27|55->28|55->28|55->28|56->29|56->29|56->29|57->30|57->30|58->31|59->32|60->33|65->38
                  -- GENERATED --
              */
          