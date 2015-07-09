
package views.html.bootstrap3

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
object checkboxes extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template4[Field,String,Map[String, Boolean],String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(field: Field, label: String = "CHANGEME", checkboxMap: Map[String, Boolean], help:String = ""):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.97*/("""

  """),format.raw/*3.3*/("""<div class="form-group """),_display_(/*3.27*/if(field.hasErrors)/*3.46*/ {_display_(Seq[Any](format.raw/*3.48*/("""has-error""")))}),format.raw/*3.58*/("""">
    """),format.raw/*4.77*/("""
    """),format.raw/*5.5*/("""<label class="control-label" for=""""),_display_(/*5.40*/field/*5.45*/.id),format.raw/*5.48*/("""">"""),_display_(/*5.51*/label),format.raw/*5.56*/("""</label>
    """),format.raw/*6.47*/("""
    """),format.raw/*7.5*/("""<div class="col-sm-10" id=""""),_display_(/*7.33*/field/*7.38*/.id),format.raw/*7.41*/("""">
      """),_display_(/*8.8*/for((checkboxName, isChecked) <- checkboxMap) yield /*8.53*/ {_display_(Seq[Any](format.raw/*8.55*/("""
        """),format.raw/*9.9*/("""<label class="checkbox">
          <input
            type="checkbox"
            name=""""),_display_(/*12.20*/(field.name + "[]")),format.raw/*12.39*/(""""
            id=""""),_display_(/*13.18*/checkboxName),format.raw/*13.30*/(""""
            value=""""),_display_(/*14.21*/checkboxName),format.raw/*14.33*/(""""
            """),_display_(/*15.14*/if(isChecked)/*15.27*/ {_display_(Seq[Any](format.raw/*15.29*/("""checked""")))}),format.raw/*15.37*/(""">
          """),_display_(/*16.12*/checkboxName),format.raw/*16.24*/("""
        """),format.raw/*17.9*/("""</label>
      """)))}),format.raw/*18.8*/("""
      """),format.raw/*19.7*/("""<span class="help-block">"""),_display_(/*19.33*/help),format.raw/*19.37*/("""</span>
      <span class="help-block">"""),_display_(/*20.33*/{field.error.map { error => error.message }}),format.raw/*20.77*/("""</span>
    </div>
  </div>
"""))}
  }

  def render(field:Field,label:String,checkboxMap:Map[String, Boolean],help:String): play.twirl.api.HtmlFormat.Appendable = apply(field,label,checkboxMap,help)

  def f:((Field,String,Map[String, Boolean],String) => play.twirl.api.HtmlFormat.Appendable) = (field,label,checkboxMap,help) => apply(field,label,checkboxMap,help)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:32:21 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/bootstrap3/checkboxes.scala.html
                  HASH: ea7c97da2552fe714f38f2b4e9780864790a377e
                  MATRIX: 773->1|956->96|986->100|1036->124|1063->143|1102->145|1142->155|1176->234|1207->239|1268->274|1281->279|1304->282|1333->285|1358->290|1398->345|1429->350|1483->378|1496->383|1519->386|1554->396|1614->441|1653->443|1688->452|1804->541|1844->560|1890->579|1923->591|1972->613|2005->625|2047->640|2069->653|2109->655|2148->663|2188->676|2221->688|2257->697|2303->713|2337->720|2390->746|2415->750|2482->790|2547->834
                  LINES: 26->1|29->1|31->3|31->3|31->3|31->3|31->3|32->4|33->5|33->5|33->5|33->5|33->5|33->5|34->6|35->7|35->7|35->7|35->7|36->8|36->8|36->8|37->9|40->12|40->12|41->13|41->13|42->14|42->14|43->15|43->15|43->15|43->15|44->16|44->16|45->17|46->18|47->19|47->19|47->19|48->20|48->20
                  -- GENERATED --
              */
          