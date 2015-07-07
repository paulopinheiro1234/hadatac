
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
object instrument_browser extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/( documentMap : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._

Seq[Any](format.raw/*1.25*/("""

"""),format.raw/*4.1*/("""
"""),_display_(/*5.2*/main("Hierarchies")/*5.21*/ {_display_(Seq[Any](format.raw/*5.23*/("""
    """),format.raw/*6.5*/("""<div class="container-fluid">
      
      <div class="row">
        """),format.raw/*9.50*/("""
		  """),_display_(/*10.6*/metadata_navigation("")),format.raw/*10.29*/("""
		  """),format.raw/*11.5*/("""<div class="table-responsive">
			 <div id="query" data-results=""""),_display_(/*12.36*/documentMap),format.raw/*12.47*/(""""></div>
			 <div id="body"></div>
    		 """),format.raw/*14.112*/("""
			 """),format.raw/*15.5*/("""<script type="text/javascript" src="http://mbostock.github.io/d3/talk/20111018/d3/d3.js"></script>
    		 <script type="text/javascript" src="http://mbostock.github.io/d3/talk/20111018/d3/d3.layout.js"></script>
    		 <script type="text/javascript" src="/assets/javascripts/treeRenderer.js"></script>  					   
    		 <link type="text/css" rel="stylesheet" href="/assets/stylesheets/treeRenderer.css"/>   					   
    	  </div>
      </div>
    </div>

""")))}),format.raw/*23.2*/("""
"""))}
  }

  def render(documentMap:String): play.twirl.api.HtmlFormat.Appendable = apply(documentMap)

  def f:((String) => play.twirl.api.HtmlFormat.Appendable) = (documentMap) => apply(documentMap)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Tue Jul 07 14:17:06 EDT 2015
                  SOURCE: /Users/paulopinheiro/Documents/workspace/metadata-browser/app/views/instrument_browser.scala.html
                  HASH: 90be6ba3a279cca1d546a2ff11ace38212aabe88
                  MATRIX: 736->1|862->24|890->43|917->45|944->64|983->66|1014->71|1110->181|1142->187|1186->210|1218->215|1311->281|1343->292|1414->438|1446->443|1931->898
                  LINES: 26->1|29->1|31->4|32->5|32->5|32->5|33->6|36->9|37->10|37->10|38->11|39->12|39->12|41->14|42->15|50->23
                  -- GENERATED --
              */
          