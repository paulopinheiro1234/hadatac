
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
object portal extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/():play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._

Seq[Any](format.raw/*1.4*/("""

"""),format.raw/*4.1*/("""
"""),_display_(/*5.2*/main("HADataC: Human-Aware Data Collection Framework")/*5.56*/ {_display_(Seq[Any](format.raw/*5.58*/("""
    """),format.raw/*6.5*/("""<div class="container-fluid" style="position: center">
      <h2>HADataC: Human-Aware Data Collection Framework</h2>
      <div class="row" style="width: 350px">
        <p>
       <h3>Main Options</h3> 
          	<a href=""""),_display_(/*11.22*/routes/*11.28*/.Application.index()),format.raw/*11.48*/("""" class="btn btn-primary btn-block" role="button">Faceted Search of Measurement Collection</a>
          	<a href=""""),_display_(/*12.22*/routes/*12.28*/.Spatial.index()),format.raw/*12.44*/("""" class="btn btn-primary btn-block" role="button">Spatial Search of Measurement Collection</a>
       		<a href=""""),_display_(/*13.20*/routes/*13.26*/.Metadata.index()),format.raw/*13.43*/("""" class="btn btn-primary btn-block" role="button">Browse Metadata Collection</a>
        </p>
      </div>
    </div>

""")))}),format.raw/*18.2*/("""
"""))}
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:32:21 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/portal.scala.html
                  HASH: 65d221c6fba903fb4302e86178bda03b79708aa8
                  MATRIX: 717->1|821->3|849->22|876->24|938->78|977->80|1008->85|1260->310|1275->316|1316->336|1459->452|1474->458|1511->474|1652->588|1667->594|1705->611|1855->731
                  LINES: 26->1|29->1|31->4|32->5|32->5|32->5|33->6|38->11|38->11|38->11|39->12|39->12|39->12|40->13|40->13|40->13|45->18
                  -- GENERATED --
              */
          