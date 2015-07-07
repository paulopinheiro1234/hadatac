
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
object metadata_table extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[models.SparqlQueryResults,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(documentMap : models.SparqlQueryResults):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.43*/("""

    """),format.raw/*3.5*/("""<h4 class="sub-header">Results</h4>
    <div class="table-responsive">
        <table class="table table-striped">
            <thead>
                 <tr>
                    """),_display_(/*8.22*/for(x <- documentMap.vars) yield /*8.48*/ {_display_(Seq[Any](format.raw/*8.50*/("""
                    """),format.raw/*9.21*/("""<th>"""),_display_(/*9.26*/x),format.raw/*9.27*/("""</th>
                    """)))}),format.raw/*10.22*/("""
                 """),format.raw/*11.18*/("""</tr>
            </thead>

            <tbody>
                """),_display_(/*15.18*/for(triple <- documentMap.triples_list) yield /*15.57*/ {_display_(Seq[Any](format.raw/*15.59*/("""
                """),format.raw/*16.17*/("""<tr>
                    """),_display_(/*17.22*/for((binding,value) <- triple.items) yield /*17.58*/{_display_(Seq[Any](format.raw/*17.59*/("""
                    """),format.raw/*18.21*/("""<td>
                        <p>"""),_display_(/*19.29*/value),format.raw/*19.34*/("""</p>
                    </td>
                    """)))}),format.raw/*21.22*/("""
                """),format.raw/*22.17*/("""</tr>
                """)))}),format.raw/*23.18*/("""
            """),format.raw/*24.13*/("""</tbody>

        </table>
    </div>"""))}
  }

  def render(documentMap:models.SparqlQueryResults): play.twirl.api.HtmlFormat.Appendable = apply(documentMap)

  def f:((models.SparqlQueryResults) => play.twirl.api.HtmlFormat.Appendable) = (documentMap) => apply(documentMap)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 01 10:37:05 EDT 2015
                  SOURCE: /Users/paulopinheiro/Documents/workspace/metadata-browser/app/views/metadata_table.scala.html
                  HASH: ccde64f4013d66d491e3f70e69f1b6168c1273f9
                  MATRIX: 751->1|880->42|912->48|1116->226|1157->252|1196->254|1244->275|1275->280|1296->281|1354->308|1400->326|1492->391|1547->430|1587->432|1632->449|1685->475|1737->511|1776->512|1825->533|1885->566|1911->571|1994->623|2039->640|2093->663|2134->676
                  LINES: 26->1|29->1|31->3|36->8|36->8|36->8|37->9|37->9|37->9|38->10|39->11|43->15|43->15|43->15|44->16|45->17|45->17|45->17|46->18|47->19|47->19|49->21|50->22|51->23|52->24
                  -- GENERATED --
              */
          