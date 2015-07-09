
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
object table extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[models.QueryResults,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(documentMap : models.QueryResults):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.37*/("""

    """),format.raw/*3.5*/("""<h2 class="sub-header">Results</h2>
    <div class="table-responsive">
        <table class="table table-striped">
            <thead>
              <tr>
                <th>Document</th>
              </tr>
            </thead>

            <tbody>
                """),_display_(/*13.18*/for(document <- documentMap.the_docs) yield /*13.55*/ {_display_(Seq[Any](format.raw/*13.57*/("""
                """),format.raw/*14.17*/("""<tr>
                        <td>
                        """),_display_(/*16.26*/for(characteristic <- document.characteristic) yield /*16.72*/{_display_(Seq[Any](format.raw/*16.73*/("""
                				"""),format.raw/*17.21*/("""<p>characteristic: """),_display_(/*17.41*/characteristic),format.raw/*17.55*/("""<p>
                		""")))}),format.raw/*18.20*/("""
                        
                		"""),_display_(/*20.20*/for((field, value) <- document.fields) yield /*20.58*/{_display_(Seq[Any](format.raw/*20.59*/("""
                            """),format.raw/*21.29*/("""<p>"""),_display_(/*21.33*/field),format.raw/*21.38*/(""": """),_display_(/*21.41*/value),format.raw/*21.46*/("""</p>
                    	""")))}),format.raw/*22.23*/("""
                    	"""),format.raw/*23.22*/("""</td>
                </tr>
                """)))}),format.raw/*25.18*/("""
            """),format.raw/*26.13*/("""</tbody>

        </table>
    </div>


"""))}
  }

  def render(documentMap:models.QueryResults): play.twirl.api.HtmlFormat.Appendable = apply(documentMap)

  def f:((models.QueryResults) => play.twirl.api.HtmlFormat.Appendable) = (documentMap) => apply(documentMap)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:01:57 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/table.scala.html
                  HASH: d364dd33979cc38fc663eba8162d6f2a8df65993
                  MATRIX: 736->1|859->36|891->42|1185->309|1238->346|1278->348|1323->365|1409->424|1471->470|1510->471|1559->492|1606->512|1641->526|1695->549|1767->594|1821->632|1860->633|1917->662|1948->666|1974->671|2004->674|2030->679|2088->706|2138->728|2214->773|2255->786
                  LINES: 26->1|29->1|31->3|41->13|41->13|41->13|42->14|44->16|44->16|44->16|45->17|45->17|45->17|46->18|48->20|48->20|48->20|49->21|49->21|49->21|49->21|49->21|50->22|51->23|53->25|54->26
                  -- GENERATED --
              */
          