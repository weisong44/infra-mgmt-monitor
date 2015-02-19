# infra-mgmt-monitor

This project is to investigate infrastructure management monitoring solution, the application
monitors a potentially large number of applications, which report metrics periodically and events
in real time. Both reports and events are in the same format. An application is typically a UNIX
process, which may contain multiple modules. Each module may decide to report its module separately,
however information must be organized. Therefore a tree-like structure is used:

	domain/host/application/module*

Each application has a main module already implemented, it serves as the "root" module. All submodules
defined by application are put under this module, and a module is allowed to contain other modules.

In this project, the upstream reporting is done using log files. An application will include an 
monitoring agent, which writes to a predefined log file. Below steps describe how log files are
further collected

1. Application writes monitoring data (reports, events) to a predefined log file
2. A local Flume agent watches and sends content of this log file to a remote Flume collector
3. The remote Flume collector receives monitoring data from many Flume agents, and sends them 
to a Kafka topic "logs"
4. A set of Kafka consumers read monitoring data and save them in InfluxDB; In InfluxDB, one database
is used; each module is defined as a serie using its path described above. Note: each serie may
contain multiple metrics (columns)





* It is assumed a monitoring application monitors 

