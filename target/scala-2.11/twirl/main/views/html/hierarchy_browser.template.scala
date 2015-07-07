
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
object hierarchy_browser extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[String,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(documentMap : String, category : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._

Seq[Any](format.raw/*1.43*/("""

"""),format.raw/*4.1*/("""
"""),_display_(/*5.2*/main("Hierarchies")/*5.21*/ {_display_(Seq[Any](format.raw/*5.23*/("""
    """),format.raw/*6.5*/("""<div class="container-fluid">
      
      <div class="row">
        """),format.raw/*9.50*/("""
		  """),_display_(/*10.6*/metadata_navigation(category)),format.raw/*10.35*/("""
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

  def render(documentMap:String,category:String): play.twirl.api.HtmlFormat.Appendable = apply(documentMap,category)

  def f:((String,String) => play.twirl.api.HtmlFormat.Appendable) = (documentMap,category) => apply(documentMap,category)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Tue Jul 07 14:17:54 EDT 2015
                  SOURCE: /Users/paulopinheiro/Documents/workspace/metadata-browser/app/views/hierarchy_browser.scala.html
                  HASH: e731553b35bc0a693e4da9e89ee5da69eb99ab2b
                  MATRIX: 742->1|886->42|914->61|941->63|968->82|1007->84|1038->89|1134->199|1166->205|1216->234|1248->239|1341->305|1373->316|1444->462|1476->467|1961->922
                  LINES: 26->1|29->1|31->4|32->5|32->5|32->5|33->6|36->9|37->10|37->10|38->11|39->12|39->12|41->14|42->15|50->23
                  -- GENERATED --
              */
          