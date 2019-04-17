2018-08-20/fki Reviewed for version 13.
27-jan-2011/FK
03-feb-2009/FK
07-nov-2006/FK

--- 
Note: on August 2018 lab TAG was reviewed, modernized and brought
into version 13. This led to fewer files, simpler software, and
cleaner code. The following changes took place:

- The Deedee example agent was removed.

- The special properties 'user' and 'room' were removed from the
Bailiff.

- Bailiff and Dexter now have id and info strings that can be set
from the commandline. These are for better diagnostics on the
console.

- Bailiff and Dexter now print commandline help and exit if they see
'?', -h, or -help on the commandline.

- Bailiff and Dexter had all of their Swing GUI removed.

- The custom util package was removed. Bailiff and Dexter now parse their
commandline arguments themselves.

- Bailiff now uses java.util.logging.Logger instead of the old custom
Logger.

- Dexter now maintains a jump counter, which is printed with the debug
messages.

- The old requirement to use Java level 1.4 was removed. Compilation
is now at Java 8. The rmic tool is no longer used.

- The package name was changed from 'dsv.pis.gotag' to just 'tag'.

- Build and run files for Windows (develop/bat, test/bin/pc) were
  reviewed, debugged, and refactored.

---

This is lab TAG, the game of Tag for mobile agents. It is highly
recommended that a good understanding of lab CHAT is acquired before
TAG is attempted.

This distribution contains the working source code for:

tag.bailiff	  A Jini-aware execution service for mobile Java code.
tag.dexter	  A small agent that jumps randomly between Bailiffs


Compiling, building, and installing is all managed by the file
build.xml, which is an ant script. Ant is a free software build tool
which can be found at

    http://ant.apache.org/

When ant is successfully installed, open a command prompt/shell and
change to the develop directory. Typing 

    ant

at the command prompt should compile the sources and compose the
jar-files. The directories build and dist are created to hold the
generated files. The build directory holds the Java class files and the
dist (distribution) contains the jar-files.

To install the jar-files in the test branch of the software tree, type
the command

    ant install

which copies the needed files over to test directories.

Sometimes it is valuable to make sure everything has been recompiled
and rebuilt fresh. To remove all generated files (your sources are not
touched), give the command

    ant clean

The above three commands are targets that can be found at the end of
the build.xml file.


On a Windows system you can compile and install by using the BAT-files
in the develop/bat directory, and run the system from the BAT-files in
the test/bin/pc directory.


			  The TAG assignment

The challenge is to create the Game of Tag for mobile software agents.

The game of tag is a child's game, played minimally by two players. It
is very simple. One player is choosen to be 'it'. That player's task
is to run up to one of the other players and touch him or her, saying
"You're it!", whereupon the 'it' property and the associated task is
transferred to the other player. The players who are currently not
'it', try to evade being tagged, usually by running and hiding.

Starting from the provided software, you must design and implement the
game of tag for at least three mobile agents. The playfield consists
of three or more Bailiffs (execution servers).

You can modify the given sources freely, and add classes and
interfaces as desired.

There are two important requirements on which the game's
implementation depend:

 (1) tagging can only be done between players in the same Bailiff

 (2) the tag (the 'it' property) must be passed reliably from one
     player to another. It must not be lost or duplicated during the
     transaction.

From the given code (Dexter) we see how the Jini middleware can help
each player to dynamically discover and communicate with Bailiffs.

From (1) we realise that a player needs to know how the other players
are distributed among the Bailiffs. The player being 'it' needs to
find a Bailiff with players in it, move there, select a victim, and
then attempt to tag that victim. The other players all want to know
where the player being 'it' is located, so that they can avoid being
tagged, by moving to another Bailiff if the player being 'it' arrives
in their Bailiff.

So, here is what is needed:

  (a) Each player can be either 'it' or 'not it', and depending on
      what it currently is, its behaviour changes appropriately.

  (b) A player needs cooperation from the Bailiffs so that is can:
      (1) Get a list of players currently located in a Bailiff
      (2) Query each player if they are 'it' or not.
      (3) Try to tag a player

  (c) From (b) it follows that each player must have a unique id, so
  that it can:
      (1) Recognise itself in a list of players (b.1)
      (2) Specify some other player (b.2 and b.3)

  (d) Finally, there must be some mechanism that determines exactly if
      a player can be tagged or not. This has to do with the way
      mobility actually works. When a mobile object (e.g. a player)
      'moves' from one Bailiff to another, it does not really move at
      all. It sends a copy of itself to the other Bailiff, and if the
      copy was successful, the original object terminates its thread
      of execution and goes to garbage collection. Thus, if a player
      is tagged at the wrong moment, the copy of the player that moved
      away stays untagged, while the original object instance is
      tagged instead. When the original instance dies, it takes the
      'it' property with it into oblivion, and the tag is lost from
      the game.

There are several possible implementation strategies, and work can be
divided between players and Bailiffs to reduce the workload of the
players. This is a design choice, but you will need to expand the
BailiffInterface to support the game, in particular b.1, b.2 and
b.3. That said, the list returned in b.1 could contain additional
information beside player id:s; for example, 'this is you' and 'this
player currently is it'. That would reduce the number of additional
remote method calls that a player otherwise would have to make.

Other possible services offered by the Bailiff could be a predicate if
it contains the player being it, or a function that returns the
current number of players in it. The number of new methods in the
Bailiff required to implement the game is usually no more than three,
depending on the solution design.


Let the Bailiff mediate player-to-player communication

It is strongly advised that communication between players use the
Bailiff as a proxy. The central message between players in the game of
Tag is of course the tagging action itself. It could look something
like this:


  // In this code example, AgentID shold be replaced with whatever is
  // chosen as the identifier datatype. The class java.util.UUID is a
  // good choice.


  // In the BailiffInterface, which is what a player sees:
  public boolean tag(AgentID aid) throws java.rmi.RemoteException;


  // Which the Bailiff implements thus:
  public boolean tag(AgentID aid) throws java.rmi.RemoteException
  {
    Agent a = lookup(aid);
    return a.tag();
  }


  // In the player when being tagged:
  public boolean tag()
  {
    if (/* tag succeeded */)
      // update my state to be 'it'
      return true;
    else
      return false;
  }
      
  // In the player when trying to tag another player:
  ...
  BailiffInterface myBailiff // Bailiff I am in
  AgentID victim             // ID of other player to tag
  ...
  try {
    if (myBailiff.tag(victim))
      // update my state to be not it
  }
  catch (RemoteException rex) {}
  

In the above example, the Bailiff uses the provided AgentID to locate
the requested player, and then perform the local method call. Then it
is the player being tagged that decides if it was tagged or not, and
returns true or false accordingly. If the requested agent is no longer
available in the Bailiff, the lookup will return null, and the calling
player receives a NullPointerException wrapped inside a
RemoteException.

It should also be realised, that when the Bailiff is calling the
player's tag() method, the player's own thread of execution is also
active in the player object. So if the player is using, for example, a
boolean variable to keep track of its 'it' status, the code that
checks that variable for what to do and how to behave, should expect
that status to go from 'not it' to 'it' at any time.

Using the Bailiff as a message proxy is good, but it is not the only
way. Other messaging schemes could be devised. However, there is a
particularly dangerous bug-fest luring in another modification of the
BailiffInterface. In that design, the Bailiff provides a reference to
the requested player, and the tagging player calls the victim directly
(except it does not):

  /* *** DO NOT USE *** */

  // BailiffInterface
  public Agent getAgent(AgentID aid) throws java.rmi.RemoteException;

  // Bailiff
  public Agent getAgent(AgentID aid) throws java.rmi.RemoteException
  {
    return lookup(aid);
  }

  // Player being it
  ...
  Agent a = myBailiff.getAgent(victimID); // get the victim
  if (a != null && a.tag())               // tag it
    isIt = false;                         // I am no longer 'it'
  ...

The programmer thinks: "Well, I know the it player and the victim are
on the same Bailiff, so I receive a reference to the victim, so I can
call its tag method to tag it." But here is the trap: the
BailiffInterface is a remote interface. It cannot return a local
reference. It will return a reference to a copy of the victim, and it
is the copy that gets tagged. The original player object is
unmodified, and so the tag is lost.

One important consequence of having the Bailiff act as mediator, is
that it must recognise the player class. Otherwise it cannot call a
Player.tag() method. This is another necessary modification of the
Bailiff, and it can be implemented in at least two ways:

The first solution is to simply use the player class in the Bailiff
code, and cast to it where required. A more elegant approach, however,
is to have the player implement an interface which the Bailiff knows
about. That way one can have players from different classes
participate in the game together.

Finally, the Bailiff will need a data structure that maps from player
identifiers (AgentID) to the local objects that are the players
themselves. The player should be added to the map when the Agitator
starts, and removed when it stops, also when the player stops because
of an exception. The data structure must be protected against
concurrent modification, because at any time a present player may die
or a remote player migrate to the Bailiff. A suitable instance of
interface java.util.Map is java.util.HashMap.

  Another bug from past years is if the Bailiff fails to remove done
  players from the map it maintains. Not only will the map continue to
  grow and consume memory, but it will also accumulate a mausoleum of
  dead player instances, which can be tagged but will never tag
  back. Once a dead player has been tagged, the tag is lost.

It must also be possible to create a list of the names of the
active players currently in the Bailiff. For the namelist, start
looking at method java.util.Map.keySet(). Do not send the whole map
instance as the response, extract the names into something nice, like
an array of String, or something which implements interface
java.util.List. If you decide to annotate the list elements with
information like, 'this is you' and 'this is the it player', you
probably want to create an element class. Remember to protect against
concurrent modification while extracting the names.



Player behaviours

The player has two hard states, being 'it' or 'not it'. In the 'it'
state, it hunts down victims and tries to tag them. A successful
predator would probably aim for the Bailiff with the most players in
it, as this is a simple heuristic to implement.

An untagged player, on the other hand, has more freedom in how to
behave. Obviously it needs to watch out for the player being 'it', and
attempt to escape if it arrives in the current Bailiff. But if not
immediately threatened, it has a choice between staying put where it
is, or hop over to some other safe Bailiff. If it decides to hop on,
it has further choices over a random Bailiff, or one with few players,
or one with many players.

The behaviour or strategy exhibited by players, will dramatically
affect the overall appearence of the game. Players that seek out
company, will huddle together in some corner of the playfield, leaving
most of the Bailiffs empty. Players that seek loneliness will spread
out, automatically balancing the load on the available Bailiffs.

There is no requirement to implement alternative non-tagged
behaviours, but you are very welcome to experiment with them and
observe the emergent system-wide effects.


Player delays

In order to be able to follow the progress of a game at human speeds
delays are required. It is difficult to give good recommendations for
these, but a general move delay of 3-5 seconds may be a good starting
point. You may also want to give each player a bit of random variation
on the order of 50-250 ms so that they slide out of lock-step
behaviour. In particular, a player being 'it' could be awarded a
slightly shorter delay to give it some edge and make successful
tagging more likely.


Player identification

Players need to have universially unique names (identifers) so that
they can be uniquely addressed. Let the agent generate its own UUID
when it starts, and then keep this name while it plays (see
java.util.UUID.randomUUID()). So, rather than having the Bailiff
assign the agent a temporary id when it arrives, the Bailiff asks the
agent what id it has.



			    Doing the work

First, study the provided sources and learn how the programs
work. Then go back to the instruction earlier in this text, and start
planning:

    - what to do
    - where to do it (i.e. which files do you need to modify)
    - how to do it (checklists, editors)
    - how to determine if it works (testing, debugging)

You will need the Bailiff, and you will need to make changes to
it. The player agent can be created by modifying Dexter, or by
creating an entirely new agent. There is no right or wrong here, but
it may be easier to recompile things if you keep Dexter and just
change its code, even if that means a lot of changes. But your are
free to make any changes you see fit, adding or dropping classes etc.

When your plan is done, make sure you can compile and run the
system BEFORE you start modifying it. 

Then implement the changes. If you can, start with something small and
confirm that it works as you intended.

When you get compilation errors, read them carefully and remember that
the first error is usually the significant one. Subsequent errors
often follow as a consequense of the first problem.

When you get run-time exceptions, read them carefully and try to
understand what is happening. Remember that exceptions are usually
nested, meaning that the critical point of failure is to be found near
the top of the list. An exception in the Bailiff will print in the
Bailiff console, in the normal way. An exception in a player also
prints in the Bailiff console, but if it is wrapped in a
RemoteException then the exception actually happened in a Bailiff,
or in a player hosted by a Bailiff:

Online documentation for the Java API is here:

       http://download.oracle.com/javase/8/docs/api/




			     Examination

Both labs are done in groups of two students. Groups with only one
member are allowed.

The grades given for the two labs combined are PASS or FAIL (godkänd
eller underkänd). The grades are individual, which means that one
group member may be passed and the other failed, if it does not seem
credible that that member did contribute to the work.

Both labs (CHAT and TAG) must be passed for a passing grade on the lab
component of the course. Work which is not up to a passing standard but
is judged to be salvagable may be required to provide additional
material before being graded.

For lab TAG you must do the following:

    - Implement the game of Tag using the provided material.

    - Write, print and submit a short report on your design choices,
      and problems encountered and solved. Remember to put the
      following on the title sheet:
      = ID2010/LAB TAG
      = Your names

    - Give an oral presentation and show a running implementation. The
      result must execute and demonstrate (textually or graphically)
      that the game is being played by three players on a playfield of
      three Bailiffs. [The group and the examiner sits down together
      at the computers.  The group runs their system and explains
      briefly what they have done. The examiner asks questions as
      necessary.]





Good luck and remember to have fun!
