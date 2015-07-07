
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
object spatial_faceting extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template10[Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,String,String,Map[String, models.SpatialQueryResults],String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(  facetForm: Form[views.formdata.FacetFormData],
    fieldFacets: models.FacetsWithCategories,
    queryFacets: models.FacetsWithCategories,
    rangeFacets: models.FacetsWithCategories,
    pivotFacets: models.FacetsWithCategories,
    clusterFacets: models.FacetsWithCategories,
    namedLocation: String,
    spatialPredicates: String, 
    documentMap : Map[String, models.SpatialQueryResults],
    final_query : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._

Seq[Any](format.raw/*10.26*/("""


"""),format.raw/*14.1*/("""
"""),_display_(/*15.2*/main("Spatial Faceted Search")/*15.32*/ {_display_(Seq[Any](format.raw/*15.34*/("""
    """),format.raw/*16.5*/("""<div class="container-fluid">
      
      <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
          """),_display_(/*20.12*/form(routes.Spatial.postIndex())/*20.44*/ {_display_(Seq[Any](format.raw/*20.46*/("""
            """),_display_(/*21.14*/spatial_fieldset(facetForm, fieldFacets, queryFacets, rangeFacets, pivotFacets, clusterFacets, namedLocation, spatialPredicates)),format.raw/*21.142*/("""
        """)))}),format.raw/*22.10*/("""
        """),format.raw/*23.9*/("""</div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1 class="page-header">Faceted Search</h1>
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

  def render(facetForm:Form[views.formdata.FacetFormData],fieldFacets:models.FacetsWithCategories,queryFacets:models.FacetsWithCategories,rangeFacets:models.FacetsWithCategories,pivotFacets:models.FacetsWithCategories,clusterFacets:models.FacetsWithCategories,namedLocation:String,spatialPredicates:String,documentMap:Map[String, models.SpatialQueryResults],final_query:String): play.twirl.api.HtmlFormat.Appendable = apply(facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,namedLocation,spatialPredicates,documentMap,final_query)

  def f:((Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,String,String,Map[String, models.SpatialQueryResults],String) => play.twirl.api.HtmlFormat.Appendable) = (facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,namedLocation,spatialPredicates,documentMap,final_query) => apply(facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,namedLocation,spatialPredicates,documentMap,final_query)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Tue Jun 30 19:00:35 EDT 2015
                  SOURCE: /Users/paulopinheiro/Documents/workspace/metadata-browser/app/views/spatial_faceting.scala.html
                  HASH: 714e2c39b90a3085a000532af37bcd06232fb575
                  MATRIX: 964->1|1493->426|1523->446|1551->448|1590->478|1630->480|1662->485|1809->605|1850->637|1890->639|1931->653|2081->781|2122->791|2158->800|2333->948|2365->959|2475->1043|2538->1090|2578->1092|2612->1099|2672->1132|2703->1142|2733->1145|2764->1155|2811->1172|2843->1177|2973->1280|3036->1327|3076->1329|3131->1356|3168->1366|3199->1376|3269->1419|3310->1439|3352->1453|3415->1485|3454->1496|3532->1544
                  LINES: 26->1|38->10|41->14|42->15|42->15|42->15|43->16|47->20|47->20|47->20|48->21|48->21|49->22|50->23|53->26|53->26|56->29|56->29|56->29|57->30|57->30|57->30|57->30|57->30|58->31|59->32|62->35|62->35|62->35|64->37|64->37|64->37|65->38|65->38|66->39|68->41|69->42|74->47
                  -- GENERATED --
              */
          