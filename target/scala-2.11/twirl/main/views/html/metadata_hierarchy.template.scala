
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
object metadata_hierarchy extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/( documentMap : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.25*/("""

    """),format.raw/*3.5*/("""<h4 class="sub-header">Results</h4>
	<div class="table-responsive">
		<div id="query" data-results=""""),_display_(/*5.34*/documentMap),format.raw/*5.45*/(""""></div>
		<div id="body"></div>
    	"""),format.raw/*7.110*/("""
		"""),format.raw/*8.3*/("""<script type="text/javascript" src="http://mbostock.github.io/d3/talk/20111018/d3/d3.js"></script>
    	<script type="text/javascript" src="http://mbostock.github.io/d3/talk/20111018/d3/d3.layout.js"></script>
    	<script type="text/javascript" src="/assets/javascripts/treeRenderer.js"></script>  					   
    	<link type="text/css" rel="stylesheet" href="/assets/stylesheets/treeRenderer.css"/>   					   
    </div>"""))}
  }

  def render(documentMap:String): play.twirl.api.HtmlFormat.Appendable = apply(documentMap)

  def f:((String) => play.twirl.api.HtmlFormat.Appendable) = (documentMap) => apply(documentMap)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:32:21 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/metadata_hierarchy.scala.html
                  HASH: 0378876c4c4c35621414f1a0cee85d65e401d9ea
                  MATRIX: 736->1|847->24|879->30|1006->131|1037->142|1103->284|1132->287
                  LINES: 26->1|29->1|31->3|33->5|33->5|35->7|36->8
                  -- GENERATED --
              */
          