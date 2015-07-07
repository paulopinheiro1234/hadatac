
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
object metadata_list extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[models.SparqlQueryResults,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/( documentMap : models.SparqlQueryResults, category : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._
import play.api.libs.json._

Seq[Any](format.raw/*1.63*/("""

"""),format.raw/*5.1*/("""
"""),_display_(/*6.2*/main("Metadata List")/*6.23*/ {_display_(Seq[Any](format.raw/*6.25*/("""
    """),_display_(/*7.6*/metadata_navigation(category)),format.raw/*7.35*/("""
    """),format.raw/*8.5*/("""<div class="container-fluid">
      
       <div class="row">
      		"""),_display_(/*11.10*/metadata_table(documentMap)),format.raw/*11.37*/("""
        """),format.raw/*12.9*/("""</div>
    </div>

""")))}),format.raw/*15.2*/("""
"""))}
  }

  def render(documentMap:models.SparqlQueryResults,category:String): play.twirl.api.HtmlFormat.Appendable = apply(documentMap,category)

  def f:((models.SparqlQueryResults,String) => play.twirl.api.HtmlFormat.Appendable) = (documentMap,category) => apply(documentMap,category)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Tue Jul 07 14:19:34 EDT 2015
                  SOURCE: /Users/paulopinheiro/Documents/workspace/metadata-browser/app/views/metadata_list.scala.html
                  HASH: b09beaf82ccae085c90c003f76f636f7d4b7b97d
                  MATRIX: 757->1|949->62|977->110|1004->112|1033->133|1072->135|1103->141|1152->170|1183->175|1281->246|1329->273|1365->282|1415->302
                  LINES: 26->1|30->1|32->5|33->6|33->6|33->6|34->7|34->7|35->8|38->11|38->11|39->12|42->15
                  -- GENERATED --
              */
          