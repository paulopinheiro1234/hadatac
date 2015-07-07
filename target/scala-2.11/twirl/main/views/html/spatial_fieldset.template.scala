
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
object spatial_fieldset extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template8[Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,String,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(  facetForm: Form[views.formdata.FacetFormData],
    fieldFacets: models.FacetsWithCategories,
    queryFacets: models.FacetsWithCategories,
    rangeFacets: models.FacetsWithCategories,
    pivotFacets: models.FacetsWithCategories,
    clusterFacets: models.FacetsWithCategories,
    namedLocation: String,
    spatialPredicates: String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import views.html.bootstrap3._

Seq[Any](format.raw/*8.31*/("""



"""),format.raw/*13.1*/("""
"""),format.raw/*14.1*/("""<fieldset class="nav nav-sidebar">
    <div id="legend">
        <legend>Facets</legend>
    </div>

    <h3>Spatial Facets</h3>
    """),_display_(/*20.6*/text(facetForm("named_geographic_location"),
          label = "Named Place",
          placeholder = "Enter a named place",
          help = "Type the name of a place you'd like to use in the search")),format.raw/*23.77*/("""

    """),format.raw/*25.5*/("""<div> 
    <h3>Field Facets</h3>
    """),_display_(/*27.6*/for((facet_type, list_of_facets) <- fieldFacets.facets) yield /*27.61*/{_display_(Seq[Any](format.raw/*27.62*/("""
        """),format.raw/*28.9*/("""<div class="col-sm-10" id="field_facets">
        """),_display_(/*29.10*/checkboxes(facetForm(facet_type),
                    label = facet_type,
                    checkboxMap = list_of_facets,
                    help = "Check zero or more facets")),format.raw/*32.56*/("""
        """),format.raw/*33.9*/("""</div>
    """)))}),format.raw/*34.6*/("""
    """),format.raw/*35.5*/("""</div>

  <div class="form-group">
    <div class="col-sm-offset-2 col-sm-10">
      <button id="submit" type="submit" value="Submit" class="btn btn-primary">Submit</button>
    </div>
  </div>
</fieldset>
"""))}
  }

  def render(facetForm:Form[views.formdata.FacetFormData],fieldFacets:models.FacetsWithCategories,queryFacets:models.FacetsWithCategories,rangeFacets:models.FacetsWithCategories,pivotFacets:models.FacetsWithCategories,clusterFacets:models.FacetsWithCategories,namedLocation:String,spatialPredicates:String): play.twirl.api.HtmlFormat.Appendable = apply(facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,namedLocation,spatialPredicates)

  def f:((Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,String,String) => play.twirl.api.HtmlFormat.Appendable) = (facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,namedLocation,spatialPredicates) => apply(facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,namedLocation,spatialPredicates)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Tue Jun 30 19:00:35 EDT 2015
                  SOURCE: /Users/paulopinheiro/Documents/workspace/metadata-browser/app/views/spatial_fieldset.scala.html
                  HASH: 190c08ea2e0414350f0b6de1eb0ecca5f160b1d2
                  MATRIX: 916->1|1373->340|1404->376|1432->377|1592->511|1814->712|1847->718|1911->756|1982->811|2021->812|2057->821|2135->872|2335->1051|2371->1060|2413->1072|2445->1077
                  LINES: 26->1|36->8|40->13|41->14|47->20|50->23|52->25|54->27|54->27|54->27|55->28|56->29|59->32|60->33|61->34|62->35
                  -- GENERATED --
              */
          