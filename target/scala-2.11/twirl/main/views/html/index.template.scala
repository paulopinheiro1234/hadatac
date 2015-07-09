
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
object index extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template9[String,Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,Map[String, models.SpatialQueryResults],String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(  username: String,
    facetForm: Form[views.formdata.FacetFormData],
    fieldFacets: models.FacetsWithCategories,
    queryFacets: models.FacetsWithCategories,
    rangeFacets: models.FacetsWithCategories,
    pivotFacets: models.FacetsWithCategories,
    clusterFacets: models.FacetsWithCategories,
    documentMap : Map[String, models.SpatialQueryResults],
    final_query : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._

Seq[Any](format.raw/*9.26*/("""


"""),format.raw/*13.1*/("""
"""),_display_(/*14.2*/main("Simple Faceted Search")/*14.31*/ {_display_(Seq[Any](format.raw/*14.33*/("""
    """),format.raw/*15.5*/("""<div class="container-fluid">
      
      <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
          """),_display_(/*19.12*/form(routes.Application.postIndex())/*19.48*/ {_display_(Seq[Any](format.raw/*19.50*/("""
            """),_display_(/*20.14*/fieldset(facetForm, fieldFacets, queryFacets, rangeFacets, pivotFacets, clusterFacets)),format.raw/*20.100*/("""
        """)))}),format.raw/*21.10*/("""
        """),format.raw/*22.9*/("""</div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1 class="page-header">Faceted Search</h1>
          <h2>"""),_display_(/*25.16*/username),format.raw/*25.24*/("""</h2>
		  <h4>"""),_display_(/*26.10*/final_query),format.raw/*26.21*/("""</h4>
		  <div class="row placeholders"></div>
		  <ul class="nav nav-tabs">
		    """),_display_(/*29.8*/for((collection, query_results) <- documentMap) yield /*29.55*/ {_display_(Seq[Any](format.raw/*29.57*/("""
		  		"""),format.raw/*30.7*/("""<li><a data-toggle="tab" href="#"""),_display_(/*30.40*/collection),format.raw/*30.50*/("""">"""),_display_(/*30.53*/collection),format.raw/*30.63*/("""</a></li>
		    """)))}),format.raw/*31.8*/("""
		  """),format.raw/*32.5*/("""</ul>
          <div class="row placeholders"></div>
          <div class = "tab-content">
          	"""),_display_(/*35.13*/for((collection, query_results) <- documentMap) yield /*35.60*/ {_display_(Seq[Any](format.raw/*35.62*/("""
          		
          			"""),format.raw/*37.14*/("""<div id=""""),_display_(/*37.24*/collection),format.raw/*37.34*/("""" class="tab-pane fade in">
          				"""),_display_(/*38.16*/table(query_results)),format.raw/*38.36*/("""
          			"""),format.raw/*39.14*/("""</div>
          		
          	""")))}),format.raw/*41.13*/("""
          """),format.raw/*42.11*/("""</div>
        </div>
      </div>
    </div>

""")))}),format.raw/*47.2*/("""
"""))}
  }

  def render(username:String,facetForm:Form[views.formdata.FacetFormData],fieldFacets:models.FacetsWithCategories,queryFacets:models.FacetsWithCategories,rangeFacets:models.FacetsWithCategories,pivotFacets:models.FacetsWithCategories,clusterFacets:models.FacetsWithCategories,documentMap:Map[String, models.SpatialQueryResults],final_query:String): play.twirl.api.HtmlFormat.Appendable = apply(username,facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,documentMap,final_query)

  def f:((String,Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,Map[String, models.SpatialQueryResults],String) => play.twirl.api.HtmlFormat.Appendable) = (username,facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,documentMap,final_query) => apply(username,facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,documentMap,final_query)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:01:57 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/index.scala.html
                  HASH: de7dac7a8dfd3b2e95b1f5d6004baa9a0f0c18ac
                  MATRIX: 945->1|1436->389|1466->409|1494->411|1532->440|1572->442|1604->447|1751->567|1796->603|1836->605|1877->619|1985->705|2026->715|2062->724|2243->878|2272->886|2314->901|2346->912|2456->996|2519->1043|2559->1045|2593->1052|2653->1085|2684->1095|2714->1098|2745->1108|2792->1125|2824->1130|2954->1233|3017->1280|3057->1282|3112->1309|3149->1319|3180->1329|3250->1372|3291->1392|3333->1406|3396->1438|3435->1449|3513->1497
                  LINES: 26->1|37->9|40->13|41->14|41->14|41->14|42->15|46->19|46->19|46->19|47->20|47->20|48->21|49->22|52->25|52->25|53->26|53->26|56->29|56->29|56->29|57->30|57->30|57->30|57->30|57->30|58->31|59->32|62->35|62->35|62->35|64->37|64->37|64->37|65->38|65->38|66->39|68->41|69->42|74->47
                  -- GENERATED --
              */
          