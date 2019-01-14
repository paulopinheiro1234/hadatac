Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/xenial64"
  config.vm.provider "virtualbox" do |vb|
       vb.name = "hadatac-virtual"
       vb.customize ["modifyvm", :id, "--memory", "6144"]
       vb.customize ["modifyvm", :id, "--cpus", "2"]
       vb.customize ["modifyvm", :id, "--clipboard", "bidirectional"]
       vb.customize ["modifyvm", :id, "--cpuexecutioncap", "80"]
       vb.customize ["modifyvm", :id, "--vram", "256"]       
  end

  config.vm.network "private_network", ip: "192.168.54.55"

  #config.ssh.forward_agent = true
  #config.ssh.pty = true
  # config.vm.network "public_network"
  # config.vm.synced_folder "../data", "/vagrant_data"

  #config.vm.provision "shell", path: "install.sh", privileged: false
  config.vm.provision "shell", privileged: false, inline: <<-SHELL
    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
    sudo add-apt-repository ppa:webupd8team/java
    sudo apt update
    sudo apt install -y git unzip sbt default-jdk wget
    cd ~
    echo "alias jetty='bash /home/vagrant/hadatac-blazegraph/jetty-distribution-9.4.12.v20180830/bin/jetty.sh'" >> .bash_aliases
    echo "alias solr='bash /home/vagrant/hadatac-solr/solr/run_solr6.sh'" >> .bash_aliases
    source .bash_aliases
    git clone https://github.com/paulopinheiro1234/hadatac.git
    cd hadatac
    ./install_hadatac.sh Y ~/hadatac-solr ~/hadatac-blazegraph
    sed \'s/com\\.bigdata\\.journal\\.AbstractJournal\\.file\\=\[\^ \]\*\\.jnl/com\\.bigdata\\.journal\\.AbstractJournal\\.file\\=\\/home\\/vagrant\\/hadatac\\-blazegraph\\/blazegraph\\.jnl/\' -i /home/vagrant/hadatac-blazegraph/jetty-distribution-9.4.12.v20180830/webapps/blazegraph/WEB-INF/classes/RWStore.properties
    bash /home/vagrant/hadatac-blazegraph/jetty-distribution-9.4.12.v20180830/bin/jetty.sh restart
    bash /home/vagrant/hadatac-solr/solr/run_solr6.sh restart
    ./create_namespaces.sh
  SHELL
end
