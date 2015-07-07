
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
object metadata extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/():play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._

Seq[Any](format.raw/*1.4*/("""

"""),format.raw/*4.1*/("""
"""),_display_(/*5.2*/main("Agent Graph")/*5.21*/ {_display_(Seq[Any](format.raw/*5.23*/("""
    """),format.raw/*6.5*/("""<div class="container-fluid">
      
      <div class="row">
		  """),_display_(/*9.6*/metadata_navigation("")),format.raw/*9.29*/("""
      """),format.raw/*10.7*/("""</div>
    </div>

""")))}),format.raw/*13.2*/("""
"""))}
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Tue Jul 07 14:18:49 EDT 2015
                  SOURCE: /Users/paulopinheiro/Documents/workspace/metadata-browser/app/views/metadata.scala.html
                  HASH: 07881dffbd98442f13db9d04bda040e453b9c4cf
                  MATRIX: 719->1|823->3|851->22|878->24|905->43|944->45|975->50|1066->116|1109->139|1143->146|1193->166
                  LINES: 26->1|29->1|31->4|32->5|32->5|32->5|33->6|36->9|36->9|37->10|40->13
                  -- GENERATED --
              */
          