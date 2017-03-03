# spring17-lab1

Contains assignment description, starter-code, and other resources for the first lab assigment.
+ Once you are done with your assignment, commit with the message **final**.
+ Points will be given for ease of reproducability and readability. So try and make it easy for us.
+ Modify this readme with instructions to run your code.
+ Comment your code for readability.
+ Source code goes into the src diretory. Tests go into tests and documentation
goes into doc


Instructions for the lab are below:

## The Problem
The birds may be angry, but the pigs are livid. They are battered and bruised by constant bird launches and have decided to be smarter in evading future bird attacks. The pigs wish to take evasive action (by moving away from an approaching bird attack) and avoid birds or stone columns from raining down on their heads. However since no pig is smart enough to achieve this goal on their own, they must resort to the wisdom of the crowds to coordinate their strategy as a group. This will be achieved using pig-to-pig (P2P) communication.
Once a bird launches, the pig closest to the bird lauch pad will estimate the trajectory and landing coordinates of that bird. The landing coordinates are communicated to the entire group using P2P messages. Each pig knows its current position and will estimate if it is impacted by the bird landing, and if so, will take evasive action by moving itself to nearby location that is not impacted by the bird launch.

Birds can launch themselves at different speeds and P2P messages incur a fixed hop-by-hop delay to propagate from one pig to another. So the evasive strategy may not always succeed if the fast-moving bird lands on the target location before the P2P messages have propagated through the pig network and all impacted pigs have taken the necessary evasive action.

Assume that the number of pigs N is specified beforehand (N should be configurable in your system).

First construct a pig-to-pig network (also known in the non-pig world as a peer-to-peer network) such that all N pigs form a connected network. You can use either a structured or unstructured P2P topology to construct the network. No neighbor discovery is needed; assume that a list of all N peers and their addresses/ports are specified in a configuration file.

Once the network is formed, prior to each bird launch, assign a new position to each pig. You can use a simple grid of coordinates and place a pig at a location on the grid. While it is fine to choose random locations, you may want to also want the ability to configure specific locations for pigs solely for purposes of testing your code. Also randomly place a few stone columns next to a few pigs. Assume that if a bird lands on a pig, a pig may fall over onto a neighboring and/or topple a neighboring stone structure, if any, which may also topple onto neighboring pigs. For simplicity, assume that if a neighbor pig fall onto a pig, that pig is hurt but it itself does not impact other neighbors. If a stone column falls onto a pig, it can the pig will further roll over onto a neighbor. In other words, you are free to limit the impact of a bird landing onto a pig and its neighbors, or the neighbor's neighbors (for falling stone columns), but no further.

The speed of a bird and its trajectory can be chosen randomly each time. However, for testing purposes, you may also want the additional ability to configure the speed and trajectory (landing coordinates) of a bird.

Finally, the delay incurred by a P2P message at each hop should be configurable. If a delay of T (in milliseconds) is specified, each peer should add a delay of T time units before propagating any of the messages listed in the interface below to the next hop(s). Note that T can be set to zero, in which case peers do not add artificial delays to simulate real network propagation delays.

## Your system should implement the following interfaces and components:
1. *bird_approaching(position,hopcount)* --  this message distributes the landing positon of a bird to the P2P network. Include a max hopcount that is decremented at each hop and the message is discarded when it reaches 0.
2. *take_shelter(pigID)* -- a pig that is impacted by the bird attack can inform its physical neighbors to take shelter. Note that the physical neighbor may not the same pigs as neighbors in a P2P network and your code should ensure that the message is delivered to physical neighbors.
3. *status(pigID)*, *status_all()* -- after a bird has landed, pigs are queried to check if they were hit or succeeded in taking evasive action. The status message can query a particular pigID or a broadcast status_all message queries all pigs.
4. *was_hit(pigID ,trueFlag)* -- A reply message to a status request reporting whether the pig was hit. These responses are used to keep "score"
5. Each pig (peer) is both a client and a server. Some messages such as bird_approaching and status_all messages are broadcast and use flooding. Status messages are requested by the pig that initiated the bird_approaching warning and replies should traverse along the reverse path back to the pig without using flooding (one way to meet this requirement is to have each peer append its peerID to a list which is propagated with the lookup message; the list yields the full path back to the first pig; other techniques are possible which involve stateful peers).

## Other requirements:
Each peer should be able to accept multiple requests at the same time. This could be easily done using threads. Be aware of the thread synchronizing issues to avoid inconsistency or deadlock in your system.

No GUIs are required. Simple command line interfaces are fine. However peers should print descriptive messages in an output log that allows a human (i.e., TA) to understand what is happening in your P2P network.

A secondary goal of this and subsequent labs is to make you familiar with modern software development practices. Familiarity with source code control systems (e.g., git, mercurial, svn) and software testing suites is now considered to be essential knowledge for a Computer Scientist. In this lab, you will use github as for source code control repository.

A related goal is to use a testing framework to test you code. You can create specific scenarios and/or inputs to test your code and verify that it works as expected. Unit testing frameworks are fine to use here, but do keep in mind that this is a distributed application with peers running on different machines. So testing is more complex in this setting. For the purposes of the lab, you should write three tests of your choice either using a testing framework or using your own scripts/inputs to test the code. The tests should be submitted along with your code.

We do not expect elaborate use of github or testing frameworks - rather we want you to become familiar with these tools and start using them for your lab work (and any other programs you write).
