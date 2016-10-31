# HADataC - Human-Aware Data Acquisition Framework

## What is HADataC?

A significant amount of time and effort is often spent organizing data before their meaning can be understood, thus enabling one to analyze the data and to infer new knowledge from them. HADataC is an infrastructure that enables combined collections of data and metadata in a way that metadata is properly and logically connected to data. By data (and metadata) collection we mean the process of identifying data sources, interacting with these sources to move the data from their transient state into a persistent repository, and to enable the data to be retrieved from their persistent repositories through the use of queries. **HADataC data** is composed of scientific measurements in support of empirical scientific activities and/or computer-generated results of model simulations in support of computational scientific activities. **HADataC metadata** is a rich collection of contextual knowledge about scientific activities encoded and connected to the data through the use of semantic web technologies. This rich metadata collection is thus leveraged by the **HADataC infrastructure** to support the following: data management; data governance in terms of privacy, access and dissemination; uncertainty management; and (big) data analytics.  

## What is it on this repository?

* /app/org/hadatac/annotator/ccsv: A semantic annotator for CSV scientific datasets
* /app/org/hadatac/console: Web applcation
* /app/org/hadatac/data/loader: A parser and loader for CCSV datasets
* /solr: Solr collections for data and metadata

## Community

Jois us on this discussion at hadatac-user-list+subscribe@googlegroups.com.

## Acknowledgements

Thanks to Andrea Gazzarini for providing valuable feedback and sharing knowledge on putting together Apache Solr and RDF triple store. Visit SolRDF project at https://github.com/agazzarini/SolRDF
