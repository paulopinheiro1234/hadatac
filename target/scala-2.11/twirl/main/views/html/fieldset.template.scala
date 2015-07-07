
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
object fieldset extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template6[Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(  facetForm: Form[views.formdata.FacetFormData],
    fieldFacets: models.FacetsWithCategories,
    queryFacets: models.FacetsWithCategories,
    rangeFacets: models.FacetsWithCategories,
    pivotFacets: models.FacetsWithCategories,
    clusterFacets: models.FacetsWithCategories):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import views.html.bootstrap3._

Seq[Any](format.raw/*6.48*/("""



"""),format.raw/*11.1*/("""
"""),format.raw/*12.1*/("""<fieldset class="nav nav-sidebar">
    <div id="legend">
        <legend>Facets</legend>
    </div>

    <div> 
    <h3>Field Facets</h3>
    """),_display_(/*19.6*/for((facet_type, list_of_facets) <- fieldFacets.facets) yield /*19.61*/{_display_(Seq[Any](format.raw/*19.62*/("""
        """),format.raw/*20.9*/("""<div class="col-sm-10" id="field_facets">
        """),_display_(/*21.10*/checkboxes(facetForm(facet_type),
                    label = facet_type,
                    checkboxMap = list_of_facets,
                    help = "Check zero or more facets")),format.raw/*24.56*/("""
        """),format.raw/*25.9*/("""</div>
    """)))}),format.raw/*26.6*/("""
    """),format.raw/*27.5*/("""</div>

  <div class="form-group">
    <div class="col-sm-offset-2 col-sm-10">
      <button id="submit" type="submit" value="Submit" class="btn btn-primary">Submit</button>
    </div>
  </div>
</fieldset>
"""))}
  }

  def render(facetForm:Form[views.formdata.FacetFormData],fieldFacets:models.FacetsWithCategories,queryFacets:models.FacetsWithCategories,rangeFacets:models.FacetsWithCategories,pivotFacets:models.FacetsWithCategories,clusterFacets:models.FacetsWithCategories): play.twirl.api.HtmlFormat.Appendable = apply(facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets)

  def f:((Form[views.formdata.FacetFormData],models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories,models.FacetsWithCategories) => play.twirl.api.HtmlFormat.Appendable) = (facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets) => apply(facetForm,fieldFacets,queryFacets,rangeFacets,pivotFacets,clusterFacets)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Tue Jun 30 19:00:34 EDT 2015
                  SOURCE: /Users/paulopinheiro/Documents/workspace/metadata-browser/app/views/fieldset.scala.html
                  HASH: 72d97a76de24e68015129706271fdfa608b5ae07
                  MATRIX: 894->1|1293->282|1324->318|1352->319|1521->462|1592->517|1631->518|1667->527|1745->578|1945->757|1981->766|2023->778|2055->783
                  LINES: 26->1|34->6|38->11|39->12|46->19|46->19|46->19|47->20|48->21|51->24|52->25|53->26|54->27
                  -- GENERATED --
              */
          