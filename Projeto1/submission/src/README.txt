Instructions for compiling and running (further described in the report):

 - In Windows:
make.bat
start rmiregistry


 - In Ubuntu:
sh compile.sh
rmiregistry &


 - Run Peer with:
java Peer <version> <peer_id> <remote_obj_name> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>


 - Run TestApp with:
java TestApp <hostname>:<remote_object_name> <sub_protocol> <opnd_1> <opnd_2>