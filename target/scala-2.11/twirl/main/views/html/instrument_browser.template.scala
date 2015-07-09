
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
object instrument_browser extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template3[Map[String, models.SparqlQueryResults],Map[String, String],String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/( documentMap : Map[String,models.SparqlQueryResults],
   documentHierarchy: Map[String,String],
   final_query : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._

Seq[Any](format.raw/*3.25*/("""

"""),format.raw/*6.1*/("""
"""),_display_(/*7.2*/main("Hierarchies")/*7.21*/ {_display_(Seq[Any](format.raw/*7.23*/("""
    """),format.raw/*8.5*/("""<div class="container-fluid">
      
      <div class="row">
        """),format.raw/*11.50*/("""
		  """),_display_(/*12.6*/metadata_navigation("")),format.raw/*12.29*/("""

        """),format.raw/*14.9*/("""<ul class="nav nav-tabs">
            <li><a data-toggle="tab" href="#InstrumentModels">View All Instrument Models</a></li>
            <li><a data-toggle="tab" href="#InstrumentModelsH">View Models as Tree</a></li>
        </ul>
        <div class = "tab-content">
        """),_display_(/*19.10*/for((thingType, query_results) <- documentMap) yield /*19.56*/ {_display_(Seq[Any](format.raw/*19.58*/("""
            """),_display_(/*20.14*/thingType/*20.23*/ match/*20.29*/{/*21.17*/case "InstrumentModels" =>/*21.43*/ {_display_(Seq[Any](format.raw/*21.45*/(""" 
                    """),format.raw/*22.21*/("""<div id="InstrumentModels" class="tab-pane fade in">
                        <div><p>(click for more information)</p></div>
                            <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                <div class="panel panel-default">
                                    """),_display_(/*26.38*/for(triple <- query_results.triples_list) yield /*26.79*/{_display_(Seq[Any](format.raw/*26.80*/("""
                                        """),_display_(/*27.42*/instrument_model_entry(triple)),format.raw/*27.72*/(""" 
                                    """)))}),format.raw/*28.38*/("""
                                """),format.raw/*29.33*/("""</div>
                            </div>
                    </div>
                """)))}/*33.17*/case _ =>/*33.26*/ {_display_(Seq[Any](format.raw/*33.28*/("""
                """)))}}),format.raw/*35.14*/("""
        """)))}),format.raw/*36.10*/("""
        """),_display_(/*37.10*/for((thingType, tree_results) <- documentHierarchy) yield /*37.61*/{_display_(Seq[Any](format.raw/*37.62*/("""
		  """),format.raw/*38.5*/("""<div class="table-responsive">
			 <div id="query" data-results=""""),_display_(/*39.36*/tree_results),format.raw/*39.48*/(""""></div>
			 <div id="body"></div>
    		 """),format.raw/*41.112*/("""
			 """),format.raw/*42.5*/("""<script type="text/javascript" src="http://mbostock.github.io/d3/talk/20111018/d3/d3.js"></script>
    		 <script type="text/javascript" src="http://mbostock.github.io/d3/talk/20111018/d3/d3.layout.js"></script>
    		 <script type="text/javascript" src="/assets/javascripts/treeRenderer.js"></script>  					   
    		 <link type="text/css" rel="stylesheet" href="/assets/stylesheets/treeRenderer.css"/>
        """)))}),format.raw/*46.10*/("""
        """),format.raw/*47.9*/("""</div>
      </div>
    </div>
    </div>
""")))}),format.raw/*51.2*/("""
"""))}
  }

  def render(documentMap:Map[String, models.SparqlQueryResults],documentHierarchy:Map[String, String],final_query:String): play.twirl.api.HtmlFormat.Appendable = apply(documentMap,documentHierarchy,final_query)

  def f:((Map[String, models.SparqlQueryResults],Map[String, String],String) => play.twirl.api.HtmlFormat.Appendable) = (documentMap,documentHierarchy,final_query) => apply(documentMap,documentHierarchy,final_query)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Thu Jul 09 11:25:10 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/instrument_browser.scala.html
                  HASH: 0abb794189169d4baf61d932bb4e778ca8bb9c52
                  MATRIX: 795->1|1019->122|1047->141|1074->143|1101->162|1140->164|1171->169|1268->279|1300->285|1344->308|1381->318|1683->593|1745->639|1785->641|1826->655|1844->664|1859->670|1869->688|1904->714|1944->716|1994->738|2360->1077|2417->1118|2456->1119|2525->1161|2576->1191|2646->1230|2707->1263|2812->1366|2830->1375|2870->1377|2920->1409|2961->1419|2998->1429|3065->1480|3104->1481|3136->1486|3229->1552|3262->1564|3333->1710|3365->1715|3809->2128|3845->2137|3918->2180
                  LINES: 26->1|31->3|33->6|34->7|34->7|34->7|35->8|38->11|39->12|39->12|41->14|46->19|46->19|46->19|47->20|47->20|47->20|47->21|47->21|47->21|48->22|52->26|52->26|52->26|53->27|53->27|54->28|55->29|58->33|58->33|58->33|59->35|60->36|61->37|61->37|61->37|62->38|63->39|63->39|65->41|66->42|70->46|71->47|75->51
                  -- GENERATED --
              */
          