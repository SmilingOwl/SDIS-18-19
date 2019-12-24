# SDIS 2018/2019

Curricular Unity: SDIS - Distributed Systems <br>
Lective Year: 2018/2019


## Project 1

Developed in collaboration with *Juliana Marques*.

We developed a distributed system to backup files, using RMI and multicast channels, in Java.

The system has the capacity to:
* **Backup files** - The initiator peer reads the file, separates it into chunks and sends them through a multicast channel so that the other peers available store it.
* **Restore files** - The initiator peer asks for the chunks of a file and the peers that have it send them through the multicast channel.
* **Delete files from the distributed system** - The initiator peer asks for the file to be deleted through the multicast channel and each peer that has a chunk deletes them.
* **Reclaim storage space** - The chunks that the peer has stored are deleted until there is enough storage space.

Concurrency is achieved with the use of threads (in particular, the class ScheduledThreadPoolExecutor).

The system includes enhancements to the following protocols:
* **Backup** - On version 2.0, instead of all the peers that receive a chunk of a file storing it, they keep track of which peers stored which chunks and store it only if there haven't been enough of peers storing it. To achieve this effect, the peers wait a random time between 0 and 400 milliseconds before storing the chunk and send a message to the multicast channel when they have stored it.
* **Restore** - On version 2.0, instead of using a multicast channel to send the chunks, the peers use TCP. In here, the peer that wants the chunk, asks for it in a message sent to the multicast channel and a peer which has the chunk initiates a conection with the other peer, through a socket, in order to send the chunk.
* **Delete** - On version 2.0, the system makes sure that a file that has been asked to be deleted will be deleted even on peers that weren't active when the request to delete the file was submitted. For that, the initiator peer keeps track of every peer that has the file chunks as well as every peer that deleted it. While there is a peer that hasn't deleted the file, the initiator peer sends a message every 30 seconds to delete it.

## Project 2

Developed in collaboration with *Juliana Marques*, *João Álvaro Ferreira* and *João Fidalgo*.

We developed a distributed backup system for the internet, in a centralized system, using TCP. There are two types of classes: Peers and Peer Managers. The Peers store the files. The Peer Managers manage the whole process.

The system supports the following actions:
* **Backup files** - The initiator peer tells the peer manager that it wants to backup a file and its desired replication degree. The Peer Manager tells him the contacts of peers that can store the file. The initiator peer initiates connections to send the file's chunks to each of the peers, which store the file and inform the peer manager that they have stored the file. 
* **Restore files** - The initiator peer tells the peer manager that it wants to restore a file, to which the peer manager responds with the contacts of the peers that hold the file. The initiator peer proceeds to communicate with the peer to receive the file.
* **Delete files** - The initiator peer informs the peer manager that he wants to delete a file, to which the peer manager responds with the contacts of all peers that hold the file. The initiator peer sends a message to each one asking for the file to be deleted.

**Concurrency** is achieved using thread-pools and non-blocking I/O operations, using *java.nio*.<br>
**Secure communication** is achieved using JSSE.<br>
**Fault Tolerance** - We covered the following scenarios:
* **Peer Manager Failure**: Every Peer holds the contacts of every Peer Manager available in the system. When a Peer Manager fails, the Peer connects to another one.
* **Peer General Failure**: The Peer Managers check every once in a while whether or not a Peer is still active. When it's not, the Peer Managers delete all the Peer's information. When the Peer comes back, it informs the Peer Managers about all the files that it has stored.
* **Peer Failure on Restore Protocol**: When the peer that is sending a file in the Restore Protocol fails, the initiator peer connects to another peer that holds the file.
