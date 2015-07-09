
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
object metadata_navigation extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.3*/(selection : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.23*/("""
 
   """),format.raw/*3.4*/("""<p>
       <B>Options</B>: 
	   """),_display_(/*5.6*/if(selection == "PlatformModelsH")/*5.40*/ {_display_(Seq[Any](format.raw/*5.42*/("""
          	"""),format.raw/*6.12*/("""<a href=""""),_display_(/*6.22*/routes/*6.28*/.Platform.index()),format.raw/*6.45*/("""" class="btn btn-warning" role="button">Platform Model<br>Hierarchy</a>
       """)))}/*7.10*/else/*7.15*/{_display_(Seq[Any](format.raw/*7.16*/("""
          	"""),format.raw/*8.12*/("""<a href=""""),_display_(/*8.22*/routes/*8.28*/.Platform.index()),format.raw/*8.45*/("""" class="btn btn-primary" role="button">Platform Model<br>Hierarchy</a>
       """)))}),format.raw/*9.9*/("""
	   """),_display_(/*10.6*/if(selection == "Platforms")/*10.34*/ {_display_(Seq[Any](format.raw/*10.36*/("""
       		"""),format.raw/*11.10*/("""<a href=""""),_display_(/*11.20*/routes/*11.26*/.PlatformList.index()),format.raw/*11.47*/("""" class="btn btn-warning" role="button">Platform<br>List</a>
       """)))}/*12.10*/else/*12.15*/{_display_(Seq[Any](format.raw/*12.16*/("""
       		"""),format.raw/*13.10*/("""<a href=""""),_display_(/*13.20*/routes/*13.26*/.PlatformList.index()),format.raw/*13.47*/("""" class="btn btn-primary" role="button">Platform<br>List</a>
       """)))}),format.raw/*14.9*/("""
	   """),_display_(/*15.6*/if(selection == "InstrumentModelsH")/*15.42*/ {_display_(Seq[Any](format.raw/*15.44*/("""
       		"""),format.raw/*16.10*/("""<a href=""""),_display_(/*16.20*/routes/*16.26*/.Instrument.index()),format.raw/*16.45*/("""" class="btn btn-warning" role="button">Instrument Model<br>Hierarchy</a>
       """)))}/*17.10*/else/*17.15*/{_display_(Seq[Any](format.raw/*17.16*/("""
       		"""),format.raw/*18.10*/("""<a href=""""),_display_(/*18.20*/routes/*18.26*/.Instrument.index()),format.raw/*18.45*/("""" class="btn btn-primary" role="button">Instrument Model<br>Hierarchy</a>
       """)))}),format.raw/*19.9*/("""
	   """),_display_(/*20.6*/if(selection == "Instruments")/*20.36*/ {_display_(Seq[Any](format.raw/*20.38*/("""
       		"""),format.raw/*21.10*/("""<a href=""""),_display_(/*21.20*/routes/*21.26*/.InstrumentList.index()),format.raw/*21.49*/("""" class="btn btn-warning" role="button">Instrument<br>List</a>
       """)))}/*22.10*/else/*22.15*/{_display_(Seq[Any](format.raw/*22.16*/("""
       		"""),format.raw/*23.10*/("""<a href=""""),_display_(/*23.20*/routes/*23.26*/.InstrumentList.index()),format.raw/*23.49*/("""" class="btn btn-primary" role="button">Instrument<br>List</a>
       """)))}),format.raw/*24.9*/("""
	   """),_display_(/*25.6*/if(selection == "DetectorsH")/*25.35*/ {_display_(Seq[Any](format.raw/*25.37*/("""
       		"""),format.raw/*26.10*/("""<a href=""""),_display_(/*26.20*/routes/*26.26*/.Detector.index()),format.raw/*26.43*/("""" class="btn btn-warning" role="button">Detector Model<br>Hierarchy</a>
       """)))}/*27.10*/else/*27.15*/{_display_(Seq[Any](format.raw/*27.16*/("""
       		"""),format.raw/*28.10*/("""<a href=""""),_display_(/*28.20*/routes/*28.26*/.Detector.index()),format.raw/*28.43*/("""" class="btn btn-primary" role="button">Detector Model<br>Hierarchy</a>
       """)))}),format.raw/*29.9*/("""
	   """),_display_(/*30.6*/if(selection == "Detectors")/*30.34*/ {_display_(Seq[Any](format.raw/*30.36*/("""
       		"""),format.raw/*31.10*/("""<a href=""""),_display_(/*31.20*/routes/*31.26*/.DetectorList.index()),format.raw/*31.47*/("""" class="btn btn-warning" role="button">Detector<br>List</a>
       """)))}/*32.10*/else/*32.15*/{_display_(Seq[Any](format.raw/*32.16*/("""
       		"""),format.raw/*33.10*/("""<a href=""""),_display_(/*33.20*/routes/*33.26*/.DetectorList.index()),format.raw/*33.47*/("""" class="btn btn-primary" role="button">Detector<br>List</a>
       """)))}),format.raw/*34.9*/("""
	   """),_display_(/*35.6*/if(selection == "EntitiesH")/*35.34*/ {_display_(Seq[Any](format.raw/*35.36*/("""
       		"""),format.raw/*36.10*/("""<a href=""""),_display_(/*36.20*/routes/*36.26*/.Entity.index()),format.raw/*36.41*/("""" class="btn btn-warning" role="button">Entity<br>Hierarchy</a>
       """)))}/*37.10*/else/*37.15*/{_display_(Seq[Any](format.raw/*37.16*/("""
       		"""),format.raw/*38.10*/("""<a href=""""),_display_(/*38.20*/routes/*38.26*/.Entity.index()),format.raw/*38.41*/("""" class="btn btn-primary" role="button">Entity<br>Hierarchy</a>
       """)))}),format.raw/*39.9*/("""
	   """),_display_(/*40.6*/if(selection == "CharacteristicsH")/*40.41*/ {_display_(Seq[Any](format.raw/*40.43*/("""
       		"""),format.raw/*41.10*/("""<a href=""""),_display_(/*41.20*/routes/*41.26*/.Characteristic.index()),format.raw/*41.49*/("""" class="btn btn-warning" role="button">Characteristic<br>Hierarchy</a>
       """)))}/*42.10*/else/*42.15*/{_display_(Seq[Any](format.raw/*42.16*/("""
       		"""),format.raw/*43.10*/("""<a href=""""),_display_(/*43.20*/routes/*43.26*/.Characteristic.index()),format.raw/*43.49*/("""" class="btn btn-primary" role="button">Characteristic<br>Hierarchy</a>
       """)))}),format.raw/*44.9*/("""
	   """),_display_(/*45.6*/if(selection == "UnitsH")/*45.31*/ {_display_(Seq[Any](format.raw/*45.33*/("""
       		"""),format.raw/*46.10*/("""<a href=""""),_display_(/*46.20*/routes/*46.26*/.Unit.index()),format.raw/*46.39*/("""" class="btn btn-warning" role="button">Unit<br>Hierarchy</a>
       """)))}/*47.10*/else/*47.15*/{_display_(Seq[Any](format.raw/*47.16*/("""
       		"""),format.raw/*48.10*/("""<a href=""""),_display_(/*48.20*/routes/*48.26*/.Unit.index()),format.raw/*48.39*/("""" class="btn btn-primary" role="button">Unit<br>Hierarchy</a>
       """)))}),format.raw/*49.9*/("""
	   """),_display_(/*50.6*/if(selection == "Agents")/*50.31*/ {_display_(Seq[Any](format.raw/*50.33*/("""
       		"""),format.raw/*51.10*/("""<a href=""""),_display_(/*51.20*/routes/*51.26*/.Agent.index()),format.raw/*51.40*/("""" class="btn btn-warning" role="button">Agent<br>Network</a>
       """)))}/*52.10*/else/*52.15*/{_display_(Seq[Any](format.raw/*52.16*/("""
       		"""),format.raw/*53.10*/("""<a href=""""),_display_(/*53.20*/routes/*53.26*/.Agent.index()),format.raw/*53.40*/("""" class="btn btn-primary" role="button">Agent<br>Network</a>
       """)))}),format.raw/*54.9*/("""
    """),format.raw/*55.5*/("""</p>
"""))}
  }

  def render(selection:String): play.twirl.api.HtmlFormat.Appendable = apply(selection)

  def f:((String) => play.twirl.api.HtmlFormat.Appendable) = (selection) => apply(selection)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:32:21 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/metadata_navigation.scala.html
                  HASH: 4582595028e8937c8561146a073db675b1a31647
                  MATRIX: 737->2|845->22|877->28|935->61|977->95|1016->97|1055->109|1091->119|1105->125|1142->142|1240->223|1252->228|1290->229|1329->241|1365->251|1379->257|1416->274|1525->354|1557->360|1594->388|1634->390|1672->400|1709->410|1724->416|1766->437|1854->507|1867->512|1906->513|1944->523|1981->533|1996->539|2038->560|2137->629|2169->635|2214->671|2254->673|2292->683|2329->693|2344->699|2384->718|2485->801|2498->806|2537->807|2575->817|2612->827|2627->833|2667->852|2779->934|2811->940|2850->970|2890->972|2928->982|2965->992|2980->998|3024->1021|3114->1093|3127->1098|3166->1099|3204->1109|3241->1119|3256->1125|3300->1148|3401->1219|3433->1225|3471->1254|3511->1256|3549->1266|3586->1276|3601->1282|3639->1299|3738->1380|3751->1385|3790->1386|3828->1396|3865->1406|3880->1412|3918->1429|4028->1509|4060->1515|4097->1543|4137->1545|4175->1555|4212->1565|4227->1571|4269->1592|4357->1662|4370->1667|4409->1668|4447->1678|4484->1688|4499->1694|4541->1715|4640->1784|4672->1790|4709->1818|4749->1820|4787->1830|4824->1840|4839->1846|4875->1861|4966->1934|4979->1939|5018->1940|5056->1950|5093->1960|5108->1966|5144->1981|5246->2053|5278->2059|5322->2094|5362->2096|5400->2106|5437->2116|5452->2122|5496->2145|5595->2226|5608->2231|5647->2232|5685->2242|5722->2252|5737->2258|5781->2281|5891->2361|5923->2367|5957->2392|5997->2394|6035->2404|6072->2414|6087->2420|6121->2433|6210->2504|6223->2509|6262->2510|6300->2520|6337->2530|6352->2536|6386->2549|6486->2619|6518->2625|6552->2650|6592->2652|6630->2662|6667->2672|6682->2678|6717->2692|6805->2762|6818->2767|6857->2768|6895->2778|6932->2788|6947->2794|6982->2808|7081->2877|7113->2882
                  LINES: 26->1|29->1|31->3|33->5|33->5|33->5|34->6|34->6|34->6|34->6|35->7|35->7|35->7|36->8|36->8|36->8|36->8|37->9|38->10|38->10|38->10|39->11|39->11|39->11|39->11|40->12|40->12|40->12|41->13|41->13|41->13|41->13|42->14|43->15|43->15|43->15|44->16|44->16|44->16|44->16|45->17|45->17|45->17|46->18|46->18|46->18|46->18|47->19|48->20|48->20|48->20|49->21|49->21|49->21|49->21|50->22|50->22|50->22|51->23|51->23|51->23|51->23|52->24|53->25|53->25|53->25|54->26|54->26|54->26|54->26|55->27|55->27|55->27|56->28|56->28|56->28|56->28|57->29|58->30|58->30|58->30|59->31|59->31|59->31|59->31|60->32|60->32|60->32|61->33|61->33|61->33|61->33|62->34|63->35|63->35|63->35|64->36|64->36|64->36|64->36|65->37|65->37|65->37|66->38|66->38|66->38|66->38|67->39|68->40|68->40|68->40|69->41|69->41|69->41|69->41|70->42|70->42|70->42|71->43|71->43|71->43|71->43|72->44|73->45|73->45|73->45|74->46|74->46|74->46|74->46|75->47|75->47|75->47|76->48|76->48|76->48|76->48|77->49|78->50|78->50|78->50|79->51|79->51|79->51|79->51|80->52|80->52|80->52|81->53|81->53|81->53|81->53|82->54|83->55
                  -- GENERATED --
              */
          