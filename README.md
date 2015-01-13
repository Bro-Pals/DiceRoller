# DiceRoller
A small java application for RPG dice rolls and practice.

DiceRoller is a simple and lightweight java app that's designed for 
people playing RPGs over the internet and want to roll some dice.

Any number of people can join the server at once, with every client being 
shown the same result from somebody's roll after they roll it. The app also 
displays all the dice rolled, the results of each dice, the total modifier in 
the roll, the person who rolled it, and the result. Everything is shown in 
a simple interface.

## Rolling

Rolling is easy! You simply type what you're rolling in the text field and 
press enter. The format you give your roll in is the same format most RPG books 
write it out, following the format of ``number +/- number`` for adding and subtracting 
modifiers and ``+/- numberOfDice 'd' sizeOfDice`` for entering dice.

Typing ``1d20`` rolls one 20 sided dice; typing ``2d10+3`` rolls two 10 sided 
dice and adds a modifier of 3. 

Typing a "1" in front of dice is optional; entering ``d10+d4-d6`` adds a 10 and 4 
sided dice and subtracts one 6 sided dice.

If the entry has multiple modifiers, DiceRoller will add them all together, so
entering``10-4+4d8-1+5`` is the same as entering ``4d8+10``

Any invalid input will be ignored by the server.

## Connecting

To connect, there must be a single server running. You start a server by running the DiceRoller 
jar inside of the ``server`` folder. 

When you start the server, it will display the port number of the server. For others to connect, you 
must port forward (ugh!) that port number to the device running the DiceRoller server. Of course, if you're 
just connecting over a LAN you don't need to port forward.

Finally, people can connect to the server! To connect, a client will need to enter in your public IP address 
if they're connecting with port forwarding or the ip address of the device running the server if you didn't 
use port forwarding. After giving a valid username, the client is all set!

To disconnect from the server, the client simply exits out of the app window. To stop the server, exit 
out of the server window.
