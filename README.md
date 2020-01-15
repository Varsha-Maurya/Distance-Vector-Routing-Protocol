# Distance-Vector-Routing-Protocol
In this project, I have implemented distance vector routing protocol. Program will be running either at several different machines (preferred) or in a single machine (simulation) . At each router, the input to your program is the set of directly attached links and their costs. Note that the program at each host doesn't know the complete network topology. Your routing program at each router should report the cost and the next hop for the shortest paths to all other routers i n the network. Instead of implementing the exact distance vector routing protocol described in the textbook, you are asked to implement a variation of the protocol. In this protocol, each host sends out the routing information to its neighbors at a certa in frequency (once every 15 seconds), regardless whether the information has changed since the last announcement. This strategy improves the robustness of the protocol. For instance, a lost message will be automatically recovered by later messages. In this strategy, typically a host re - computes its distance vector and routing table right before sending out the routing information to its neighbors. Since we don't have real network routers to test the routing algorithm , your programs will run on your laptop o r desktop . However, if your routing program runs well on a set of desktop machines, it will also likely to work on real network routers. As specified in the distance vector protocols, your routing program will exchange the routing information with directly connected neighbors. Real routing protocols use UDP for such exchanges. Under such a scheme, each host should listen at a UDP port for incoming routing messages.