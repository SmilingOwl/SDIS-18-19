Instructions for compiling and running (further described in the report):

 
- In Windows:

make.bat

start rmiregistry


 

- In Ubuntu:

sh compile.sh

rmiregistry &




 - Run first PeerManager with:

java -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password -Djavax.net.ssl.trustStore=truststore.ts -Djavax.net.ssl.trustStorePassword=password PeerManager <port>

  <port> is the port that serves as access point for communications with the PeerManager

 - Run remaining PeerManager with:


java -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password -Djavax.net.ssl.trustStore=truststore.ts -Djavax.net.ssl.trustStorePassword=password PeerManager <port> <manager_ip> <manager_port>

  <port> is the port that serves as access point for communications with the PeerManager
  <manager_ip> is the ip address or hostname of another PeerManager that already belongs to the system
  <manager_port> is the port of the other PeerManager

 - Run Peer with:

java -Djavax.net.ssl.trustStore=truststore.ts -Djavax.net.ssl.trustStorePassword=password -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password Peer <id> <remote_object_name> <port> <manager_ip> <manager_port>

  <id> is an integer that identifies the peer
  <remote_object_name> is the the remote object's name, used for RMI
  <port> is the port that will be open for communication with the peer
  <manager_ip> is the ip address or hostname of a PeerManager that belongs to the system
  <manager_port> is the port of the PeerManager

- Run TestApp with:


java TestApp <hostname>:<remote_object_name> <sub_protocol> <file_name> <rep_degree>

  <hostname> is the hostname of the Peer that the application is trying to connect to
  <remote_object_name> is the Peer's remote object's name
  <sub_protocol> is either BACKUP, RESTORE or DELETE
  <rep_degree> is the file's deesired replication degree for the BACKUP protocol. It's only used for this protocol.