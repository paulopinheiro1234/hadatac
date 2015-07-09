
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
object agent_browser extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[String,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/( documentMap : String, category : String):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import helper._

Seq[Any](format.raw/*1.44*/("""

"""),format.raw/*4.1*/("""
"""),_display_(/*5.2*/main("Agent Graph")/*5.21*/ {_display_(Seq[Any](format.raw/*5.23*/("""
    """),format.raw/*6.5*/("""<div class="container-fluid">
      
      <div class="row">
        """),format.raw/*9.50*/("""
		  """),_display_(/*10.6*/metadata_navigation(category)),format.raw/*10.35*/("""
		  """),format.raw/*11.5*/("""<div class="table-responsive">
			 <div id="query" data-results=""""),_display_(/*12.36*/documentMap),format.raw/*12.47*/(""""></div>
			 <div id="body"></div>
             <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js"></script>
			 """),format.raw/*15.107*/("""
    		 """),format.raw/*16.117*/("""
             """),format.raw/*17.14*/("""<script src="http://labratrevenge.com/d3-tip/javascripts/d3.tip.v0.6.3.js"></script>
    		 <script type="text/javascript" src="/assets/javascripts/forceFieldRenderer.js"></script>  					   
    		 <link type="text/css" rel="stylesheet" href="/assets/stylesheets/forceFieldRenderer.css"/>   					   
    	  </div>
      </div>
    </div>

""")))}),format.raw/*24.2*/("""
"""))}
  }

  def render(documentMap:String,category:String): play.twirl.api.HtmlFormat.Appendable = apply(documentMap,category)

  def f:((String,String) => play.twirl.api.HtmlFormat.Appendable) = (documentMap,category) => apply(documentMap,category)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed Jul 08 15:01:57 EDT 2015
                  SOURCE: /home/tetherless/hadatac-browser/app/views/agent_browser.scala.html
                  HASH: b28854899a3b20bde7bad7496320feaeee695c4d
                  MATRIX: 738->1|883->43|911->62|938->64|965->83|1004->85|1035->90|1131->200|1163->206|1213->235|1245->240|1338->306|1370->317|1533->553|1570->670|1612->684|1982->1024
                  LINES: 26->1|29->1|31->4|32->5|32->5|32->5|33->6|36->9|37->10|37->10|38->11|39->12|39->12|42->15|43->16|44->17|51->24
                  -- GENERATED --
              */
          