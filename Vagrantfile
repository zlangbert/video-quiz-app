# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  config.vm.box = "ubuntu/trusty64"

  config.vm.network "forwarded_port", guest: 3306, host: 3307

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "1024"
  end

  config.vm.provision "shell", inline: <<-SHELL
    sudo apt-get update
    sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password password root'
    sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password_again password root'
    sudo apt-get update
    sudo apt-get -y install mysql-server
    sed -i "s/^bind-address/#bind-address/" /etc/mysql/my.cnf
    mysql -u root -proot -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION; FLUSH PRIVILEGES;"
    sudo service mysql restart

    mysql -u root -proot -e "CREATE USER 'video_quizzes'@'%' IDENTIFIED BY 'video_quizzes';"
    mysql -u root -proot -e "CREATE DATABASE video_quizzes;"
    mysql -u root -proot -e "GRANT ALL PRIVILEGES ON video_quizzes.* TO 'video_quizzes'@'%'; FLUSH PRIVILEGES;"
  SHELL
end
