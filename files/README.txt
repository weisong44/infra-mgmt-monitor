Flume host setup
================
1. Install Ubuntu
2. Extract host-flume-home-dir.tar.gz to home directory
3. Do the following for each set

> cd ~/workspace/set1/bin
> ./start-all.sh

Kafka host setup
================
1. Install Ubuntu
2. Combine the splitted files, and extract to home directory

> cat *aa *ab *ac *ad > host-kafka-home-dir.tar.gz
> tar xvfz host-kafka-home-dir.tar.gz
> mv workspace ~

3. Do the following for each set1

> cd
> workspace/set1/bin/start_zookeeper.sh

4. Do the following for each set

> cd
> workspace/set1/bin/start_all.sh
> workspace/set1/bin/start_console_consumer.sh

Monitor host setup
==================
1. Install Ubuntu
2. Install InfluxDB(Web GUI at port 8083, RESTful at port 8086)

> wget http://s3.amazonaws.com/influxdb/influxdb_latest_amd64.deb
> sudo dpkg -i influxdb_latest_amd64.deb
> sudo service influxdb start

3. Install Grafana

> cd /tmp; wget http://grafanarel.s3.amazonaws.com/grafana-1.9.1.zip
> cd /opt; sudo tar xvfz /tmp/grafana-1.9.1.zip
> cd /opt/grafana-1.9.1
> cp config.sample.js config.js
> vi config.js

Edit data sources section

datasources: {
    influxdb: {
        type: 'influxdb',
        url: "http://192.168.1.27:8086/db/influxdb",
        username: 'weisong',
        password: 'songwei',
    },
    grafana: {
        type: 'influxdb',
        url: "http://192.168.1.27:8086/db/grafana",
        username: 'weisong',
        password: 'songwei',
        grafanaDB: true
    },
},

> sudo apt-get install apache2
> cat > /tmp/grafana.conf <<EOF
Alias /grafana /opt/grafana-1.9.1
 
<Location /grafana>
Order deny,allow
Allow from 127.0.0.1
Allow from ::1
Allow from all
</Location>
EOF
 
> sudo mv /tmp/grafana.conf /etc/apache2/conf.d/grafana.conf
> sudo service apache2 restart

4. Point browser to http://localhost/grafana
5. Extract host-monitor-home-dir.tar.gz to home directory
6. Do the following for each set1

> cd
> workspace/set1/bin/start_all.sh

Install Oracle JDK
==================
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java7-installer

vi /etc/bash.bashrc
JAVA_HOME=/usr

HBase setup
===========
Download from
http://apache.cs.utah.edu/hbase/hbase-0.98.10.1/

mkdir ~/data/hbase
mkdir ~/data/zookeeper

Edit hbase-site.xml

<configuration>
  <property>
    <name>hbase.rootdir</name>
    <value>file:///home/weisong/data/hbase</value>
  </property>
  <property>
    <name>hbase.zookeeper.property.dataDir</name>
    <value>/home/weisong/data/zookeeper</value>
  </property>
</configuration>


OpenTSDB setup
==============
1. Installation
sudo apt-get install autoconf
git clone git://github.com/OpenTSDB/opentsdb.git
cd opentsdb
./build.sh

2. Create tables
env COMPRESSION=NONE HBASE_HOME=/home/weisong/tools/hbase ./src/create_table.sh

3. Configuration
mkdir -p ~/data/opentsdb/static
mkdir -p ~/data/pentsdb/cache

vi src/opentsdb.conf
tsd.http.staticroot = /home/weisong/data/opentsdb/static
tsd.network.port = 4242
tsd.http.cachedir = /home/weisong/data/opentsdb/cache
tsd.storage.hbase.zk_quorum = localhost
tsd.core.auto_create_metrics = true
tsd.http.request.cors_domains=http://192.168.59.5:8000

4. Start OpenTSDB
cd src
../build/tsdb tsd

Grafana setup
=============
1. Download
http://grafanarel.s3.amazonaws.com/grafana-1.9.1.tar.gz

2. Configuration
cd conf
cp config.sample.js config.js
vi config.js

      datasources: {
        opentsdb: {
          type: 'opentsdb',
          url: "http://192.168.59.5:4242",
        },
        grafana: {
          type: 'elasticsearch',
          url: "http://192.168.59.5:9200",
          index: 'grafana-dash',
          grafanaDB: true
        },
      },

3. Start
cd ~/tools/grafana
python -m SimpleHTTPServer

