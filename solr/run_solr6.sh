SOLR_INSTALL_DIR=$HADATAC_SOLR/solr-6.5.0

if [ ! -d "$SOLR_INSTALL_DIR" ]; then
  echo "$SOLR_INSTALL_DIR not found! Please check the SOLR_INSTALL_DIR setting in your $0 script."
  exit 1
fi


SOLR_ENV=$HADATAC_SOLR/solr6.in.sh

if [ ! -f "$SOLR_ENV" ]; then
  echo "$SOLR_ENV not found! Please check the SOLR_ENV setting in your $0 script."
  exit 1
fi

case "$1" in
  start|stop|restart|status)
    SOLR_CMD=$1
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit
esac

SOLR_INCLUDE=$SOLR_ENV $SOLR_INSTALL_DIR/bin/solr $SOLR_CMD -force
