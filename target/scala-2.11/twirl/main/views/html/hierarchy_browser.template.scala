
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
object hierarchy_browser extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template3[Map[String, models.SparqlQueryResults],Map[String, String],String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/( documentMap : Map[String,models.SparqlQueryResults],
   documentHierarchy : Map[String,String],
   category : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._

Seq[Any](format.raw/*3.22*/("""

"""),format.raw/*6.1*/("""
"""),_display_(/*7.2*/main("Hierarchies")/*7.21*/ {_display_(Seq[Any](format.raw/*7.23*/("""
    """),format.raw/*8.5*/("""<div class="container-fluid">
      
      <div class="row">
        <div class="col-sm-9 col-md-10 main">
		  """),_display_(/*12.6*/metadata_navigation(category)),format.raw/*12.35*/("""
            """),format.raw/*13.13*/("""<ul class="nav nav-tabs">
                """),_display_(/*14.18*/for((thingType, query_results) <- documentMap) yield /*14.64*/{_display_(Seq[Any](format.raw/*14.65*/("""
                    """),format.raw/*15.21*/("""<li><a data-toggle="tab" href="#"""),_display_(/*15.54*/thingType),format.raw/*15.63*/("""">View as Table</a></li>
                """)))}),format.raw/*16.18*/("""
                """),_display_(/*17.18*/for((thingType, tree_results) <- documentHierarchy) yield /*17.69*/{_display_(Seq[Any](format.raw/*17.70*/("""
                    """),format.raw/*18.21*/("""<li><a data-toggle="tab" href="#"""),_display_(/*18.54*/thingType),format.raw/*18.63*/("""">View as Hierarchy</a></li>
                """)))}),format.raw/*19.18*/("""
            """),format.raw/*20.13*/("""</ul>
            <div class = "tab-content">
            """),_display_(/*22.14*/for((thingType, query_results) <- documentMap) yield /*22.60*/ {_display_(Seq[Any](format.raw/*22.62*/("""
                """),_display_(/*23.18*/thingType/*23.27*/ match/*23.33*/{/*24.21*/case "InstrumentModels" =>/*24.47*/ {_display_(Seq[Any](format.raw/*24.49*/(""" 
                        """),format.raw/*25.25*/("""<div id=""""),_display_(/*25.35*/thingType),format.raw/*25.44*/("""" class="tab-pane fade in">
                            <div><p>(click for more information)</p></div>
                                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                    <div class="panel panel-default">
                                        """),_display_(/*29.42*/for(triple <- query_results.triples_list) yield /*29.83*/{_display_(Seq[Any](format.raw/*29.84*/("""
                                            """),_display_(/*30.46*/instrument_model_entry(triple)),format.raw/*30.76*/(""" 
                                        """)))}),format.raw/*31.42*/("""
                                    """),format.raw/*32.37*/("""</div>
                                </div>
                        </div>
                    """)))}/*36.21*/case "Instruments" =>/*36.42*/ {_display_(Seq[Any](format.raw/*36.44*/(""" 
                        """),format.raw/*37.25*/("""<div id=""""),_display_(/*37.35*/thingType),format.raw/*37.44*/("""" class="tab-pane fade in">
                            <div><p>(click for more information)</p></div>
                                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                    <div class="panel panel-default">
                                        """),_display_(/*41.42*/for(triple <- query_results.triples_list) yield /*41.83*/{_display_(Seq[Any](format.raw/*41.84*/("""
                                            """),_display_(/*42.46*/instrument_entry(triple)),format.raw/*42.70*/(""" 
                                        """)))}),format.raw/*43.42*/("""
                                    """),format.raw/*44.37*/("""</div>
                                </div>
                        </div>
                    """)))}/*48.21*/case "DetectorModels" =>/*48.45*/ {_display_(Seq[Any](format.raw/*48.47*/(""" 
                        """),format.raw/*49.25*/("""<div id=""""),_display_(/*49.35*/thingType),format.raw/*49.44*/("""" class="tab-pane fade in">
                            <div><p>(click for more information)</p></div>
                                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                    <div class="panel panel-default">
                                        """),_display_(/*53.42*/for(triple <- query_results.triples_list) yield /*53.83*/{_display_(Seq[Any](format.raw/*53.84*/("""
                                            """),format.raw/*54.79*/("""
                                            """),_display_(/*55.46*/placeholder_entry(triple)),format.raw/*55.71*/("""
                                        """)))}),format.raw/*56.42*/("""
                                    """),format.raw/*57.37*/("""</div>
                                </div>
                        </div>
                    """)))}/*61.21*/case "Detectors" =>/*61.40*/ {_display_(Seq[Any](format.raw/*61.42*/(""" 
                        """),format.raw/*62.25*/("""<div id=""""),_display_(/*62.35*/thingType),format.raw/*62.44*/("""" class="tab-pane fade in">
                            <div><p>(click for more information)</p></div>
                                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                    <div class="panel panel-default">
                                        """),_display_(/*66.42*/for(triple <- query_results.triples_list) yield /*66.83*/{_display_(Seq[Any](format.raw/*66.84*/("""
                                            """),_display_(/*67.46*/detector_entry(triple)),format.raw/*67.68*/(""" 
                                        """)))}),format.raw/*68.42*/("""
                                    """),format.raw/*69.37*/("""</div>
                                </div>
                        </div>
                    """)))}/*73.21*/case "PlatformModels" =>/*73.45*/ {_display_(Seq[Any](format.raw/*73.47*/(""" 
                        """),format.raw/*74.25*/("""<div id=""""),_display_(/*74.35*/thingType),format.raw/*74.44*/("""" class="tab-pane fade in">
                            <div><p>(click for more information)</p></div>
                                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                    <div class="panel panel-default">
                                        """),_display_(/*78.42*/for(triple <- query_results.triples_list) yield /*78.83*/{_display_(Seq[Any](format.raw/*78.84*/("""
                                            """),_display_(/*79.46*/platform_model_entry(triple)),format.raw/*79.74*/(""" 
                                        """)))}),format.raw/*80.42*/("""
                                    """),format.raw/*81.37*/("""</div>
                                </div>
                        </div>
                    """)))}/*85.21*/case "Platforms" =>/*85.40*/ {_display_(Seq[Any](format.raw/*85.42*/(""" 
                        """),format.raw/*86.25*/("""<div id=""""),_display_(/*86.35*/thingType),format.raw/*86.44*/("""" class="tab-pane fade in">
                            <div><p>(click for more information)</p></div>
                                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                    <div class="panel panel-default">
                                        """),_display_(/*90.42*/for(triple <- query_results.triples_list) yield /*90.83*/{_display_(Seq[Any](format.raw/*90.84*/("""
                                            """),format.raw/*91.73*/("""
                                            """),_display_(/*92.46*/placeholder_entry(triple)),format.raw/*92.71*/("""
                                        """)))}),format.raw/*93.42*/("""
                                    """),format.raw/*94.37*/("""</div>
                                </div>
                        </div>
                    """)))}/*98.21*/case "Entities" =>/*98.39*/ {_display_(Seq[Any](format.raw/*98.41*/(""" 
                        """),format.raw/*99.25*/("""<div id=""""),_display_(/*99.35*/thingType),format.raw/*99.44*/("""" class="tab-pane fade in">
                            <div><p>(click for more information)</p></div>
                                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                    <div class="panel panel-default">
                                        """),_display_(/*103.42*/for(triple <- query_results.triples_list) yield /*103.83*/{_display_(Seq[Any](format.raw/*103.84*/("""
                                            """),format.raw/*104.71*/("""
                                            """),_display_(/*105.46*/placeholder_entry(triple)),format.raw/*105.71*/("""
                                        """)))}),format.raw/*106.42*/("""
                                    """),format.raw/*107.37*/("""</div>
                                </div>
                        </div>
                    """)))}/*111.21*/case "Characteristics" =>/*111.46*/ {_display_(Seq[Any](format.raw/*111.48*/(""" 
                        """),format.raw/*112.25*/("""<div id=""""),_display_(/*112.35*/thingType),format.raw/*112.44*/("""" class="tab-pane fade in">
                            <div><p>(click for more information)</p></div>
                                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                    <div class="panel panel-default">
                                        """),_display_(/*116.42*/for(triple <- query_results.triples_list) yield /*116.83*/{_display_(Seq[Any](format.raw/*116.84*/("""
                                            """),format.raw/*117.79*/("""
                                            """),_display_(/*118.46*/placeholder_entry(triple)),format.raw/*118.71*/("""
                                        """)))}),format.raw/*119.42*/("""
                                    """),format.raw/*120.37*/("""</div>
                                </div>
                        </div>
                    """)))}/*124.21*/case "Units" =>/*124.36*/ {_display_(Seq[Any](format.raw/*124.38*/(""" 
                        """),format.raw/*125.25*/("""<div id=""""),_display_(/*125.35*/thingType),format.raw/*125.44*/("""" class="tab-pane fade in">
                            <div><p>(click for more information)</p></div>
                                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
                                    <div class="panel panel-default">
                                        """),_display_(/*129.42*/for(triple <- query_results.triples_list) yield /*129.83*/{_display_(Seq[Any](format.raw/*129.84*/("""
                                            """),format.raw/*130.69*/("""
                                            """),_display_(/*131.46*/placeholder_entry(triple)),format.raw/*131.71*/("""
                                        """)))}),format.raw/*132.42*/("""
                                    """),format.raw/*133.37*/("""</div>
                                </div>
                        </div>
                    """)))}/*137.21*/case _ =>/*137.30*/ {_display_(Seq[Any](format.raw/*137.32*/("""
                    """)))}}),format.raw/*139.18*/("""
            """)))}),format.raw/*140.14*/("""
            """),_display_(/*141.14*/for((thingType, tree_results) <- documentHierarchy) yield /*141.65*/{_display_(Seq[Any](format.raw/*141.66*/("""
                """),format.raw/*142.17*/("""<div id=""""),_display_(/*142.27*/thingType),format.raw/*142.36*/("""" class="tab-pane fade in">
                    <div id="query" data-results=""""),_display_(/*143.52*/tree_results),format.raw/*143.64*/(""""></div>
                    <div id="body"></div>
                    """),format.raw/*145.125*/("""
                    """),format.raw/*146.21*/("""<script type="text/javascript" src="http://mbostock.github.io/d3/talk/20111018/d3/d3.js"></script>
                    <script type="text/javascript" src="http://mbostock.github.io/d3/talk/20111018/d3/d3.layout.js"></script>
                    <script type="text/javascript" src="/assets/javascripts/treeRenderer.js"></script>  					   
                    <link type="text/css" rel="stylesheet" href="/assets/stylesheets/treeRenderer.css"/>   					   
                </div>
            """)))}),format.raw/*151.14*/("""
          """),format.raw/*152.11*/("""</div>
    </div>

""")))}),format.raw/*155.2*/("""
"""))}
  }

  def render(documentMap:Map[String, models.SparqlQueryResults],documentHierarchy:Map[String, String],category:String): play.twirl.api.HtmlFormat.Appendable = apply(documentMap,documentHierarchy,category)

  def f:((Map[String, models.SparqlQueryResults],Map[String, String],String) => play.twirl.api.HtmlFormat.Appendable) = (documentMap,documentHierarchy,category) => apply(documentMap,documentHierarchy,category)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Thu Jul 09 14:04:13 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/hierarchy_browser.scala.html
                  HASH: 56d881d56372444f395a8aff710383e3920c25ca
                  MATRIX: 794->1|1016->120|1044->139|1071->141|1098->160|1137->162|1168->167|1306->279|1356->308|1397->321|1467->364|1529->410|1568->411|1617->432|1677->465|1707->474|1780->516|1825->534|1892->585|1931->586|1980->607|2040->640|2070->649|2147->695|2188->708|2274->767|2336->813|2376->815|2421->833|2439->842|2454->848|2464->870|2499->896|2539->898|2593->924|2630->934|2660->943|3017->1273|3074->1314|3113->1315|3186->1361|3237->1391|3311->1434|3376->1471|3493->1590|3523->1611|3563->1613|3617->1639|3654->1649|3684->1658|4041->1988|4098->2029|4137->2030|4210->2076|4255->2100|4329->2143|4394->2180|4511->2299|4544->2323|4584->2325|4638->2351|4675->2361|4705->2370|5062->2700|5119->2741|5158->2742|5231->2821|5304->2867|5350->2892|5423->2934|5488->2971|5605->3090|5633->3109|5673->3111|5727->3137|5764->3147|5794->3156|6151->3486|6208->3527|6247->3528|6320->3574|6363->3596|6437->3639|6502->3676|6619->3795|6652->3819|6692->3821|6746->3847|6783->3857|6813->3866|7170->4196|7227->4237|7266->4238|7339->4284|7388->4312|7462->4355|7527->4392|7644->4511|7672->4530|7712->4532|7766->4558|7803->4568|7833->4577|8190->4907|8247->4948|8286->4949|8359->5022|8432->5068|8478->5093|8551->5135|8616->5172|8733->5291|8760->5309|8800->5311|8854->5337|8891->5347|8921->5356|9279->5686|9337->5727|9377->5728|9451->5799|9525->5845|9572->5870|9646->5912|9712->5949|9830->6068|9865->6093|9906->6095|9961->6121|9999->6131|10030->6140|10388->6470|10446->6511|10486->6512|10560->6591|10634->6637|10681->6662|10755->6704|10821->6741|10939->6860|10964->6875|11005->6877|11060->6903|11098->6913|11129->6922|11487->7252|11545->7293|11585->7294|11659->7363|11733->7409|11780->7434|11854->7476|11920->7513|12038->7632|12057->7641|12098->7643|12153->7683|12199->7697|12241->7711|12309->7762|12349->7763|12395->7780|12433->7790|12464->7799|12571->7878|12605->7890|12706->8065|12756->8086|13278->8576|13318->8587|13369->8607
                  LINES: 26->1|31->3|33->6|34->7|34->7|34->7|35->8|39->12|39->12|40->13|41->14|41->14|41->14|42->15|42->15|42->15|43->16|44->17|44->17|44->17|45->18|45->18|45->18|46->19|47->20|49->22|49->22|49->22|50->23|50->23|50->23|50->24|50->24|50->24|51->25|51->25|51->25|55->29|55->29|55->29|56->30|56->30|57->31|58->32|61->36|61->36|61->36|62->37|62->37|62->37|66->41|66->41|66->41|67->42|67->42|68->43|69->44|72->48|72->48|72->48|73->49|73->49|73->49|77->53|77->53|77->53|78->54|79->55|79->55|80->56|81->57|84->61|84->61|84->61|85->62|85->62|85->62|89->66|89->66|89->66|90->67|90->67|91->68|92->69|95->73|95->73|95->73|96->74|96->74|96->74|100->78|100->78|100->78|101->79|101->79|102->80|103->81|106->85|106->85|106->85|107->86|107->86|107->86|111->90|111->90|111->90|112->91|113->92|113->92|114->93|115->94|118->98|118->98|118->98|119->99|119->99|119->99|123->103|123->103|123->103|124->104|125->105|125->105|126->106|127->107|130->111|130->111|130->111|131->112|131->112|131->112|135->116|135->116|135->116|136->117|137->118|137->118|138->119|139->120|142->124|142->124|142->124|143->125|143->125|143->125|147->129|147->129|147->129|148->130|149->131|149->131|150->132|151->133|154->137|154->137|154->137|155->139|156->140|157->141|157->141|157->141|158->142|158->142|158->142|159->143|159->143|161->145|162->146|167->151|168->152|171->155
                  -- GENERATED --
              */
          