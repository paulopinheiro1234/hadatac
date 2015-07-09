
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
object hierarchy_fieldset extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template8[Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,String,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(  facetForm: Form[views.formdata.FacetFormData],
    fieldFacets: models.FacetsWithCategories,
    queryFacets: models.FacetsWithCategories,
    rangeFacets: models.FacetsWithCategories,
    pivotFacets: models.FacetsWithCategories,
    clusterFacets: models.FacetsWithCategories,
    thingType: String,
    predicate: String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import views.html.bootstrap3._

Seq[Any](format.raw/*8.23*/("""



"""),format.raw/*13.1*/("""
"""),format.raw/*14.1*/("""<fieldset class="nav nav-sidebar">
    <div id="legend">
        <legend>Facets</legend>
    </div>

    <div> 
    <h3>Field Facets</h3>
    """),_display_(/*21.6*/for((facet_type, list_of_facets) <- fieldFacets.facets) yield /*21.61*/{_display_(Seq[Any](format.raw/*21.62*/("""
        """),format.raw/*22.9*/("""<div class="col-sm-10" id="field_facets">
        """),_display_(/*23.10*/checkboxes(facetForm(facet_type),
                    label = facet_type,
                    checkboxMap = list_of_facets,
                    help = "Check zero or more facets")),format.raw/*26.56*/("""
        """),format.raw/*27.9*/("""</div>
    """)))}),format.raw/*28.6*/("""
    """),format.raw/*29.5*/("""</div>

  <div class="form-group">
    <div class="col-sm-offset-2 col-sm-10">
      <button id="submit" type="submit" value="Submit" class="btn btn-primary">Submit</button>
    </div>
  </div>
</fieldset>
"""))}
  }

  def render(facetForm:Form[views.formdata.FacetFormData],fieldFacets:models.FacetsWithCategories,queryFacets:models.FacetsWithCategories,rangeFacets:models.FacetsWithCategories,pivotFacets:models.FacetsWithCategories,clusterFacets:models.FacetsWithCategories,thingType:String,predicate:String): play.twirl.api.HtmlFormat.Appendable = apply(facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,thingType,predicate)

  def f:((Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,String,String) => play.twirl.api.HtmlFormat.Appendable) = (facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,thingType,predicate) => apply(facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets,thingType,predicate)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:32:21 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/hierarchy_fieldset.scala.html
                  HASH: f5ae033ef15c69e7647d50454bb36e15fbff1d3a
                  MATRIX: 918->1|1363->328|1394->364|1422->365|1591->508|1662->563|1701->564|1737->573|1815->624|2015->803|2051->812|2093->824|2125->829
                  LINES: 26->1|36->8|40->13|41->14|48->21|48->21|48->21|49->22|50->23|53->26|54->27|55->28|56->29
                  -- GENERATED --
              */
          