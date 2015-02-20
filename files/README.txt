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

