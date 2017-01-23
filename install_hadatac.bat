cls
ECHO OFF
ECHO === Welcome to HADataC - The Human-Aware Data Acquisition Framework ===
ECHO 
ECHO   The following wizard will guide you into deploying a working
ECHO instance of HADataC on your machine. Please refer to
ECHO https://github.com/paulopinheiro1234/hadatac/wiki if you have any
ECHO questions about this installation.
ECHO 
ECHO   ATTENTION:
ECHO   1) This script downloads and install Apache Solr. This takes around
ECHO      300Mbytes of data. Make sure you have a decent connection and
ECHO      this data availability.
ECHO 

set /p response= "Proceed with installation? [y/N]" 

IF %response% == N EXIT
IF %response% == n EXIT

SET HADATAC_HOME=c:\hadatac

set /p resp2= "Directory of installation [c:\hadatac]: "

IF "%resp2%" NEQ "" (SET HADATAC_HOME=%resp2%)

ECHO %HADATAC_HOME%

SET HADATAC_DOWNLOAD=%HADATAC_HOME%\download
SET HADATAC_SOLR=%HADATAC_HOME%\solr
SET SOLR5_HOME=%HADATAC_SOLR%\solr-5.2.1

mkdir %HADATAC_HOME%
mkdir %HADATAC_DOWNLOAD%
mkdir %HADATAC_SOLR%
MKDIR %HADATAC_DOWNLOAD%\jts-1.13

:: copy recuservely from current dir to %HADATAC_HOME%

ECHO === Downloading Apache Solr 5.2.1...
wget -O %HADATAC_DOWNLOAD%\solr-5.2.1.zip http://archive.apache.org/dist/lucene/solr/5.2.1/solr-5.2.1.zip
wget -O %HADATAC_DOWNLOAD%\solr-5.2.1.zip.md5 http://archive.apache.org/dist/lucene/solr/5.2.1/solr-5.2.1.zip.md5
ECHO === Downloading JTS Topology Suite 1.13...
wget -O %HADATAC_DOWNLOAD%\jts-1.13.zip http://pilotfiber.dl.sourceforge.net/project/jts-topo-suite/jts/1.13/jts-1.13.zip

ECHO === Uncompressing Apache Solr 5.2.1...
winrar x %HADATAC_DOWNLOAD%\solr-5.2.1.zip *.* %HADATAC_SOLR%
ECHO === Uncompressing JTS Topology Suite 1.13...
winrar x %HADATAC_DOWNLOAD%\jts-1.13.zip *.* %HADATAC_DOWNLOAD%\jts-1.13

ECHO "HADATAC_SOLR=%HADATAC_SOLR%" >> %HADATAC_SOLR%\hadatac_solr.bat
copy /b %HADATAC_SOLR%\hadatac_solr.bat+%HADATAC_SOLR%\solr5.in.bat %HADATAC_SOLR%\solr5.in.bat

ECHO "HADATAC_SOLR=%HADATAC_SOLR%" >> %HADATAC_SOLR%\hadatac_solr.bat
copy /b %HADATAC_SOLR%\hadatac_solr.bat+%HADATAC_SOLR%\solr5.in.bat %HADATAC_SOLR%\solr5.in.bat

%HADATAC_SOLR%\run_solr5.bat start

copy $HADATAC_DOWNLOAD\jts-1.13\lib\* $HADATAC_SOLR\solr-5.2.1\server\solr-webapp\webapp\WEB-INF\lib\

%HADATAC_SOLR%\run_solr5.bat restart

