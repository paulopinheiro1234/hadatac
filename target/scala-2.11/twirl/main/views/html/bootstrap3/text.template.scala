
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
object text extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template4[Field,String,String,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(field: Field, label: String = "CHANGEME", placeholder: String = "", help: String = ""):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.89*/("""

  """),format.raw/*3.3*/("""<div class="form-group """),_display_(/*3.27*/if(field.hasErrors)/*3.46*/ {_display_(Seq[Any](format.raw/*3.48*/("""has-error""")))}),format.raw/*3.58*/("""">
    <label class="control-label">"""),_display_(/*4.35*/label),format.raw/*4.40*/("""</label>
    <div class="col-sm-10">
      <input type="text" 
             class="form-control" 
             id=""""),_display_(/*8.19*/field/*8.24*/.id),format.raw/*8.27*/("""" 
             name=""""),_display_(/*9.21*/field/*9.26*/.name),format.raw/*9.31*/("""" 
             value=""""),_display_(/*10.22*/field/*10.27*/.value.getOrElse("")),format.raw/*10.47*/(""""
             placeholder=""""),_display_(/*11.28*/placeholder),format.raw/*11.39*/("""" />
      <span class="help-block">"""),_display_(/*12.33*/help),format.raw/*12.37*/("""</span>
      <span class="help-block">"""),_display_(/*13.33*/{field.error.map { error => error.message }}),format.raw/*13.77*/("""</span>
    </div>
  </div>
"""))}
  }

  def render(field:Field,label:String,placeholder:String,help:String): play.twirl.api.HtmlFormat.Appendable = apply(field,label,placeholder,help)

  def f:((Field,String,String,String) => play.twirl.api.HtmlFormat.Appendable) = (field,label,placeholder,help) => apply(field,label,placeholder,help)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:32:21 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/bootstrap3/text.scala.html
                  HASH: 330dbf14475c3decd5a313e545609b3789a85fec
                  MATRIX: 753->1|928->88|958->92|1008->116|1035->135|1074->137|1114->147|1177->184|1202->189|1344->305|1357->310|1380->313|1429->336|1442->341|1467->346|1518->370|1532->375|1573->395|1629->424|1661->435|1725->472|1750->476|1817->516|1882->560
                  LINES: 26->1|29->1|31->3|31->3|31->3|31->3|31->3|32->4|32->4|36->8|36->8|36->8|37->9|37->9|37->9|38->10|38->10|38->10|39->11|39->11|40->12|40->12|41->13|41->13
                  -- GENERATED --
              */
          