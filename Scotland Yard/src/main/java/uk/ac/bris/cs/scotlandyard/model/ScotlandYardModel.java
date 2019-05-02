package uk.ac.bris.cs.scotlandyard.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import uk.ac.bris.cs.gamekit.graph.*;
import static uk.ac.bris.cs.scotlandyard.model.Colour.*;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Graph;

public class ScotlandYardModel implements ScotlandYardGame {
	private ArrayList < ScotlandYardPlayer > scotlandYardPlayers = new ArrayList < > ();
	private ArrayList < Colour > detectiveColours = new ArrayList < > ();
	private boolean doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder = false;
	private final Graph < Integer, Transport > graphPublic;
	private final List < Boolean > publicRounds;
	private List < PlayerConfiguration > publicPlayerConfigurations;
	private int intCurrentRound = 0;
	private int intMaxRounds;
	private ScotlandYardPlayer currentPlayer;
	private ScotlandYardPlayer publicMrXPlayer;
	private Player currentPlayerInterface;
	private int intCurrentPlayerIndex = 0;
	private int intPublicLastSeenPosition = 0;
	private Ticket ticketTemp;
	private Boolean ticketTempGranted = true;
	private ArrayList < Spectator > publicSpectators = new ArrayList < > ();
	private boolean mrXWon = false;


	public ScotlandYardModel(List < Boolean > rounds, Graph < Integer, Transport > graph,
							 PlayerConfiguration mrX, PlayerConfiguration firstDetective,
							 PlayerConfiguration...restOfTheDetectives) {


		ticketTemp = TAXI;
		intMaxRounds = rounds.size();
		publicRounds = rounds;
		graphPublic = graph;
		//Creates a list of all of our player configurations, lets us do some iteration.
		ArrayList < PlayerConfiguration > configurations = new ArrayList < > ();
		configurations.add(mrX);
		configurations.add(firstDetective);
		detectiveColours.add(firstDetective.colour);
		for (PlayerConfiguration configuration: restOfTheDetectives) {
			configurations.add(configuration);
			detectiveColours.add(configuration.colour);
		}
		publicPlayerConfigurations = configurations;
		//adds mrX to our player list
		ScotlandYardPlayer mrXPlayer = new ScotlandYardPlayer(mrX.player, mrX.colour, mrX.location, mrX.tickets);
		scotlandYardPlayers.add(mrXPlayer);
		//adds the first detective to our player list
		scotlandYardPlayers.add(new ScotlandYardPlayer(firstDetective.player, firstDetective.colour, firstDetective.location, firstDetective.tickets));
		//adds the rest of the detectives to our player list
		for (PlayerConfiguration detective: restOfTheDetectives) {
			scotlandYardPlayers.add(new ScotlandYardPlayer(detective.player, detective.colour, detective.location, detective.tickets));
		}

		publicMrXPlayer = mrXPlayer;
		validateConfigurations(mrX, rounds, graph, configurations);
		validatePlayers(scotlandYardPlayers);

		//Now let's set mrX as the first player so that he is used upon our first rotation.
		currentPlayer = mrXPlayer;
	}


	private void validateConfigurations(PlayerConfiguration mrX, List < Boolean > rounds, Graph < Integer, Transport > graph, List < PlayerConfiguration > configurations) {

		if (mrX.colour != BLACK || mrX.colour.isDetective()) throw new IllegalArgumentException("MrX should be Black... racial profiling");
		if (rounds.isEmpty()) throw new IllegalArgumentException("You aint got no rounds mate");
		if (graph.isEmpty()) throw new IllegalArgumentException("You bloody fruitshop owner has no graphs!");

		//We need to pass a list of all of the player configurations
		// This should check that nobody is in the same location.
		Set < Integer > setLocation = new HashSet < > ();
		for (PlayerConfiguration configuration: configurations) {
			if (setLocation.contains(configuration.location))
				throw new IllegalArgumentException("Duplicate location, you plonker!");
			setLocation.add(configuration.location);
		}
		// We will now perform a similar operation on our player colours.
		// This should check that nobody is of the same colour:
		Set < Colour > setColour = new HashSet < > ();
		for (PlayerConfiguration configuration: configurations) {
			if (setColour.contains(configuration.colour))
				throw new IllegalArgumentException("Duplicate colour, you willy!");
			setColour.add(configuration.colour);
		}

		//Check that all the players at least have references to their ticket types:
		for (PlayerConfiguration player: configurations) {
			for (Ticket ticket: Ticket.values()) {
				if (player.tickets.get(ticket) == null) {
					throw new IllegalArgumentException("Ticket, you willy!");
				}
			}
		}
	}


	private void validatePlayers(List < ScotlandYardPlayer > scotlandYardPlayerList) {
		// We need to set up each player with their tickets
		//Tests the tickets owned by each detective
		// We will iterate through all the players in the game and ensure that the detectives do not have any illegal tickets:
		int mrXCount = 0;
		for (ScotlandYardPlayer player: scotlandYardPlayerList) {
			if (player.hasTickets(DOUBLE) && player.isDetective()) throw new IllegalArgumentException("Yer bastidge, yer bleedin worm! By joh! No detective should have a double ticket");
			if (player.hasTickets(SECRET) && player.isDetective()) throw new IllegalArgumentException("The name's Bond... James Bond... but I shouldn't have a secret ticket");
			if (player.isMrX()) mrXCount += 1;
		}
		if (mrXCount > 1) throw new IllegalArgumentException("There's some sort of criminal ring going on, there's more than one mrX!");
	}

	@Override
	public void registerSpectator(Spectator spectator) {
		boolean canAdd = true;
		// If we pass in a null reference to a spectator, this is quite simply a bad thing and we must throw a Null pointer excetption.
		if (spectator == null) throw new NullPointerException("You aint got no spectator you bastard!");
			// Otherwise, if the spectator we pass in actually exists, we will test to ensure that no other references to the same spectator have been registered.
		else {
			//Firstly check all of the spectators currently in our collection. Make sure that we're not adding a duplicate
			for (Spectator s: publicSpectators) {
				if (s == spectator) canAdd = false;
			}
			// If all the spectators we have added are unique, we may add the new spectator to our list.
			if (canAdd) publicSpectators.add(spectator);
		}
		if (canAdd == false) throw new IllegalArgumentException("We have already added this spectator man!");
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		if (publicSpectators == null && spectator != null) throw new IllegalArgumentException();
		if (spectator == null) throw new NullPointerException("You can't unregister a spectator that doesn't exist, you fool!");
		else if (!publicSpectators.contains(spectator)) throw new IllegalArgumentException();
			//Riiight it all seems to be fine so we're gonna unregister the spectator
		else publicSpectators.remove(spectator);
	}

	@Override
	public Collection < Spectator > getSpectators() {
		return Collections.unmodifiableCollection(publicSpectators);
	}

	@Override
	//This subroutine increments the currently selected player that we will be dealing with.
	//Loops around if it reaches the end of the array.
	public void startRotate() {
		boolean blnDoNotChangeCurrentPlayer = false;
		if (isGameOver()) throw new IllegalStateException();
		else {
			intCurrentPlayerIndex = 0;
			for (ScotlandYardPlayer p: scotlandYardPlayers) {
				currentPlayer = scotlandYardPlayers.get(intCurrentPlayerIndex);
				currentPlayerInterface = publicPlayerConfigurations.get(intCurrentPlayerIndex).player;
				if (intCurrentPlayerIndex == 0 && currentPlayer.isMrX() == false) throw new RuntimeException("mrX should play first");
				startMove();
				if (isMrXCaptured()) {
					blnDoNotChangeCurrentPlayer = true;
					break;
				}
				if (intCurrentPlayerIndex < scotlandYardPlayers.size() - 1) {
					intCurrentPlayerIndex += 1; //Get ready to select the next player on the next cycle.
					currentPlayer = scotlandYardPlayers.get(intCurrentPlayerIndex);
					currentPlayerInterface = publicPlayerConfigurations.get(intCurrentPlayerIndex).player;
				}
				// if the game is over, we may break our for loop and go straight on to inform spectators
				if (isMrXCaptured()) {
					blnDoNotChangeCurrentPlayer = true;
					break;
				}
			}
			if (blnDoNotChangeCurrentPlayer == false) currentPlayer = publicMrXPlayer;
			// Firstly, check if the game is not over. If it is not, then notify the spectator that we have completed a rotation.
			// If either team has won, then notify the spectator that the game is over.
			if (isGameOver()) {
				for (Spectator s: publicSpectators) {
					s.onGameOver(this, getWinningPlayers());
				}
			} else {
				for (Spectator s: publicSpectators) {
					if (doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder == false) s.onRotationComplete(this);
				}
			}
			doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder = false;
		}
	}

	public void startMove() {
		//get the moves the player can make and return as a set
		ScotlandYardView view = this;
		Set < Move > moveSet = new HashSet < > ();
		moveSet.addAll(generateMoves());

		// Lots of our move mechanics are implemented within a lambda function that is passed as a parameter of makeMove.
		Consumer < Move > moveConsumer = move -> {
			int intDestination = 0;
			ScotlandYardPlayer commitPlayer = currentPlayer;
			String strDestination = move.toString().replaceAll("\\D+", "");
			// In this section we simply clarify whether the move is in a valid format and can be handled
			boolean validMove = true;
			if (move == null) throw new NullPointerException();
			if (move == new PassMove(commitPlayer.colour())) throw new RuntimeException("Uhh I'm lost, I have to throw a pass move");
			if (!moveSet.contains(move)) validMove = false;
			if (validMove == false) throw new IllegalArgumentException();

			if (strDestination.length() > 0) intDestination = Integer.parseInt(strDestination);
			//Pre-emptively getting a reference to the next player upon committing the move
			if (intCurrentPlayerIndex < scotlandYardPlayers.size() - 1) currentPlayer = scotlandYardPlayers.get(intCurrentPlayerIndex + 1);
			else {
				currentPlayer = publicMrXPlayer;
				intCurrentPlayerIndex = 0;
			}
			// This section of if statements deals with single moves from any player
			// ticketTemp is used to store the ticket to grant to mrX.
			// A secret ticket is not granted due to the fact that it is never used by a detective
			if (!move.toString().contains("Double")) {
				if (move.toString().contains("SECRET")) {
					ticketTempGranted = false;
					ticketTemp = SECRET;
					commitPlayer.location(intDestination);
					commitPlayer.removeTicket(ticketTemp);
				}
				if (move.toString().contains("TAXI")) {
					ticketTempGranted = true;
					ticketTemp = TAXI;
					commitPlayer.location(intDestination);
				}
				if (move.toString().contains("BUS")) {
					ticketTempGranted = true;
					ticketTemp = BUS;
					commitPlayer.location(intDestination);
				}
				if (move.toString().contains("UNDERGROUND")) {
					ticketTempGranted = true;
					ticketTemp = UNDERGROUND;
					commitPlayer.location(intDestination);
				}
				if (move.toString().contains("Pass")) {
					ticketTempGranted = false;
					commitPlayer.location(commitPlayer.location());
				}
			}


			// Alternatively, if mrX makes a double move, the whole situation is a little more compliated:
			if (move.toString().contains("Double")) {
				// We ensure that we do not give mrX a copy of the ticket he just used:
				ticketTempGranted = false;
				// We now declare two booleans. These are used to mask certain parts of the move that is sent to the spectator
				// For instance, we must hide the destination that mrX has moved to if it is currently not a reveal round.
				boolean blnHideFirstMove = false;
				boolean blnHideSecondMove = false;
				// At this point we deconstruct the move that will be made using regular expressions.
				// This allows us to get the transport types and destinations from the move.
				String[] tickets = move.toString().split("-");
				String ticketType1 = tickets[1].replaceAll("[^a-zA-Z]", "");
				String ticketType2 = tickets[3].replaceAll("[^a-zA-Z]", "");
				String[] destinations = move.toString().split("->");
				String singleDestination = destinations[1].replaceAll("[^\\d]", "");
				String doubleDestination = destinations[2].replace("]", "");
				intDestination = Integer.parseInt(doubleDestination);
				int intSingleDestination = Integer.parseInt(singleDestination);
				// Here we set ticket variables to be equal to that of in the move.
				Ticket ticket1 = TAXI;
				Ticket ticket2 = TAXI;
				if (ticketType1.contains("TAXI")) ticket1 = TAXI;
				if (ticketType1.contains("BUS")) ticket1 = BUS;
				if (ticketType1.contains("UNDERGROUND")) ticket1 = UNDERGROUND;
				if (ticketType1.contains("SECRET")) ticket1 = SECRET;
				if (ticketType2.contains("TAXI")) ticket2 = TAXI;
				if (ticketType2.contains("BUS")) ticket2 = BUS;
				if (ticketType2.contains("UNDERGROUND")) ticket2 = UNDERGROUND;
				if (ticketType2.contains("SECRET")) ticket2 = SECRET;

				commitPlayer.removeTicket(DOUBLE);

				//NOTIFY DOUBLE MOVE
				for (Spectator s: publicSpectators) {
					// We need to check whether mrX should be hidden or not
					// If he is hidden this turn, use his last known position
					// If he is hidden next turn, also use his last known position
					// if MrX may not be seen both rounds, make up a lastPos -> lastPos move containing the transport methods
					if (!publicRounds.get(getCurrentRound()) && !publicRounds.get(getCurrentRound() + 1)) {
						DoubleMove fullyHiddenDoubleMove = new DoubleMove(BLACK, ticket1, intPublicLastSeenPosition, ticket2, intPublicLastSeenPosition);
						s.onMoveMade(view, fullyHiddenDoubleMove);
						blnHideFirstMove = true;
						blnHideSecondMove = true;
					} else if (publicRounds.get(getCurrentRound()) && !publicRounds.get(getCurrentRound() + 1)) {
						DoubleMove latterHiddenDoubleMove = new DoubleMove(BLACK, ticket1, intSingleDestination, ticket2, intSingleDestination);
						s.onMoveMade(view, latterHiddenDoubleMove);
						blnHideFirstMove = false;
						blnHideSecondMove = true;
					} else if (!publicRounds.get(getCurrentRound()) && publicRounds.get(getCurrentRound() + 1)) {
						DoubleMove formerHiddenDoubleMove = new DoubleMove(BLACK, ticket1, intPublicLastSeenPosition, ticket2, intDestination);
						s.onMoveMade(view, formerHiddenDoubleMove);
						blnHideFirstMove = true;
						blnHideSecondMove = false;
					} else s.onMoveMade(view, move);
				}
				// We have just finished consuming our double move, now it's time to consume our first transport ticket:
				commitPlayer.removeTicket(ticket1);
				commitPlayer.location(intSingleDestination);
				//Increment the round
				intCurrentRound += 1;
				//START NEW ROUND
				for (Spectator s: publicSpectators) {
					s.onRoundStarted(this, intCurrentRound);
				}
				//NOTIFY THE FIRST TICKET MOVE
				for (Spectator s: publicSpectators) {
					if (!blnHideFirstMove) s.onMoveMade(view, new TicketMove(BLACK, ticket1, intSingleDestination));
					else s.onMoveMade(view, new TicketMove(BLACK, ticket1, intPublicLastSeenPosition)); //Change this to actual move
				}
				//Increment the round
				intCurrentRound += 1;
				//START NEW ROUND
				commitPlayer.removeTicket(ticket2);
				commitPlayer.location(intDestination);
				for (Spectator s: publicSpectators) {
					s.onRoundStarted(this, intCurrentRound);
				}
				//NOTIFY THE SECOND TICKET MOVE
				for (Spectator s: publicSpectators) {
					if (!blnHideSecondMove) s.onMoveMade(view, new TicketMove(BLACK, ticket2, intDestination));
					else if (!blnHideFirstMove && blnHideSecondMove) s.onMoveMade(view, new TicketMove(BLACK, ticket2, intSingleDestination));
					else if (blnHideSecondMove) s.onMoveMade(view, new TicketMove(BLACK, ticket2, intPublicLastSeenPosition));
					else s.onMoveMade(view, new TicketMove(BLACK, ticket2, intDestination));
				}
				doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder = true;
				// WE HAVE DONE WITH THE DOUBLE MOVE
			}
			// If the move we are dealing with isn't a double move or ticket, remove the ticket from the player.
			else if (ticketTempGranted && ticketTemp != SECRET) commitPlayer.removeTicket(ticketTemp);
			// If the move was made by MrX, then increment the round
			if (commitPlayer.isMrX()) {
				if (!move.toString().contains("Double")) intCurrentRound += 1;
				ticketTempGranted = false;
			} else doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder = false;


			if (!move.toString().contains("Double")) {
				if (commitPlayer == publicMrXPlayer) {
					for (Spectator s: publicSpectators) {
						s.onRoundStarted(view, intCurrentRound);
					}
				}

				if (ticketTempGranted) {
					publicMrXPlayer.addTicket(ticketTemp);
				}
				for (Spectator s: publicSpectators) {
					// if the player is mrX, then we should see if he should have to cover up his move destination.
					// this is if he is not on a reveal round.
					if (commitPlayer == publicMrXPlayer) {
						if (publicRounds.get(intCurrentRound - 1) == false) {
							TicketMove mv = new TicketMove(BLACK, ticketTemp, intPublicLastSeenPosition);
							s.onMoveMade(view, mv);
						} else s.onMoveMade(view, move);
					}
					// If we are not dealing with mrX, then just make the standard move
					else s.onMoveMade(view, move);
				}
			}
		};
		// ---------- LAMBDA FUNCTION HAS ENDED ----------------
		currentPlayerInterface.makeMove(view, currentPlayer.location(), moveSet, moveConsumer);
	}



	// Generates a set of moves by analysing each edge and connected node adjacent to our current position.
	Set < Move > generateMoves() {
		Set < Move > moveSet = new HashSet < > ();
		Collection < Edge < Integer, Transport >> connectedEdges = graphPublic.getEdgesFrom(graphPublic.getNode(currentPlayer.location()));
		for (Edge < Integer, Transport > connectedEdge: connectedEdges) {
			//firstly, check that no other player is occupying the node that we're gonna look at:
			if (!nodeOccupiedExcludingMrX(connectedEdge.destination().value())) {
				// add a preliminary secret move if the player is mrX:
				if (currentPlayer.isMrX() && currentPlayer.hasTickets(SECRET)) {
					moveSet.add(new TicketMove(currentPlayer.colour(), SECRET, connectedEdge.destination().value()));
					if (doubleMoveConditions()) moveSet.addAll(generateDoubleMoves(connectedEdge.destination().value(), (new TicketMove(currentPlayer.colour(), SECRET, connectedEdge.destination().value())))); // passing our current edge destination and our root move.
				}
				// if the edge is a taxi link
				if (connectedEdge.data() == Transport.TAXI && currentPlayer.hasTickets(TAXI)) {
					moveSet.add(new TicketMove(currentPlayer.colour(), TAXI, connectedEdge.destination().value()));
					if (doubleMoveConditions()) moveSet.addAll(generateDoubleMoves(connectedEdge.destination().value(), (new TicketMove(currentPlayer.colour(), TAXI, connectedEdge.destination().value())))); // passing our current edge destination and our root move.
				}
				// if the edge is a bus link
				if (connectedEdge.data() == Transport.BUS && currentPlayer.hasTickets(BUS)) {
					moveSet.add(new TicketMove(currentPlayer.colour(), BUS, connectedEdge.destination().value()));
					if (doubleMoveConditions()) moveSet.addAll(generateDoubleMoves(connectedEdge.destination().value(), (new TicketMove(currentPlayer.colour(), BUS, connectedEdge.destination().value())))); // passing our current edge destination and our root move.
				}
				// if the edge is a train
				if (connectedEdge.data() == Transport.UNDERGROUND && currentPlayer.hasTickets(UNDERGROUND)) {
					moveSet.add(new TicketMove(currentPlayer.colour(), UNDERGROUND, connectedEdge.destination().value()));
					if (doubleMoveConditions()) moveSet.addAll(generateDoubleMoves(connectedEdge.destination().value(), (new TicketMove(currentPlayer.colour(), UNDERGROUND, connectedEdge.destination().value())))); // passing our current edge destination and our root move.
				}
			}
		}
		//if there are no moves that can be made, produce a pass move
		if (moveSet.size() == 0) moveSet.add(new PassMove(currentPlayer.colour()));
		return moveSet;
	}
	// Simply returns whether the conditions are met for a double move set to be generated
	private Boolean doubleMoveConditions() {
		if (currentPlayer.isMrX() == true && (intCurrentRound < intMaxRounds - 2) && currentPlayer.hasTickets(DOUBLE)) return true;
		else return false;
	}
	// This method generates and returns an extension to our move set. It is called on each adjacent node to our current node.

	Set < Move > generateDoubleMoves(int nodeID, TicketMove rootMove) {
		Set < Move > doubleMoveSet = new HashSet < > ();
		TicketMove newMove;
		DoubleMove doubleMove;
		//take the ticket from a player, then give it back at the end:
		//this prevents us from using the same ticket twice in our algorithm
		currentPlayer.removeTicket(rootMove.ticket());
		//get all the possible moves from the current location
		Collection < Edge < Integer, Transport >> connectedEdges = graphPublic.getEdgesFrom(graphPublic.getNode(nodeID));
		for (Edge < Integer, Transport > connectedEdge: connectedEdges) {
			if (nodeOccupiedExcludingMrX(connectedEdge.destination().value()) == false) {
				//Firstly add a possible secret move for the edge:
				if (currentPlayer.isMrX() && currentPlayer.hasTickets(SECRET)) {
					newMove = new TicketMove(currentPlayer.colour(), SECRET, connectedEdge.destination().value());
					doubleMove = new DoubleMove(currentPlayer.colour(), rootMove, newMove);
					doubleMoveSet.add(doubleMove);
				}
				if (connectedEdge.data() == Transport.TAXI && currentPlayer.hasTickets(TAXI)) {
					newMove = new TicketMove(currentPlayer.colour(), TAXI, connectedEdge.destination().value());
					doubleMove = new DoubleMove(currentPlayer.colour(), rootMove, newMove);
					doubleMoveSet.add(doubleMove);
				}
				if (connectedEdge.data() == Transport.BUS && currentPlayer.hasTickets(BUS)) {
					newMove = new TicketMove(currentPlayer.colour(), BUS, connectedEdge.destination().value());
					doubleMove = new DoubleMove(currentPlayer.colour(), rootMove, newMove);
					doubleMoveSet.add(doubleMove);
				}
				if (connectedEdge.data() == Transport.UNDERGROUND && currentPlayer.hasTickets(UNDERGROUND)) {
					newMove = new TicketMove(currentPlayer.colour(), UNDERGROUND, connectedEdge.destination().value());
					doubleMove = new DoubleMove(currentPlayer.colour(), rootMove, newMove);
					doubleMoveSet.add(doubleMove);
				}
			}
		}
		// Let's add back the ticket otherwise that wouldn't be very fair, would it?
		currentPlayer.addTicket(rootMove.ticket());
		return doubleMoveSet;
	}
	// Checks if a node is occupied by any player.
	boolean nodeOccupied(int nodeID) {
		for (ScotlandYardPlayer player: scotlandYardPlayers) {
			if (player.location() == nodeID) return true;
		}
		return false;
	}
	// Checks if a node is occupied by a detective player.
	// This is used for determining the validity of a double move, since mrX may simply move back to his prior position.
	boolean nodeOccupiedExcludingMrX(int nodeID) {
		for (ScotlandYardPlayer player: scotlandYardPlayers) {
			if (player.location() == nodeID && player.isMrX() == false) return true;
		}
		return false;
	}

	@Override
	//Gets a list of the colours of all players currently in the game
	public List < Colour > getPlayers() {
		List < Colour > getPlayerList = new ArrayList < > ();
		for (ScotlandYardPlayer player: scotlandYardPlayers) {
			getPlayerList.add(player.colour());
		}
		final List < Colour > getPlayerListFinal = getPlayerList;
		return Collections.unmodifiableList(getPlayerListFinal);
	}

	@Override
	// Returns the winning players as a list, empty list if no one has won.
	public Set < Colour > getWinningPlayers() {
		Set < Colour > winningColours = new HashSet < > ();
		if (isGameOver()) {
			if (mrXWon) winningColours.add(BLACK);
			else {
				for (Colour colour: detectiveColours) {
					winningColours.add(colour);
				}
			}
		}
		return Collections.unmodifiableSet(winningColours);
	}

	@Override
	//Get the location of a specified player by colour. Return empty if the player is not in the game.
	public Optional < Integer > getPlayerLocation(Colour colour) {
		boolean validPlayer = false;
		for (ScotlandYardPlayer player: scotlandYardPlayers) {
			if (colour == player.colour()) validPlayer = true;
		}
		if (validPlayer == false) return Optional.empty();


		for (ScotlandYardPlayer player: scotlandYardPlayers) {
			if (player.colour() == colour) {
				if (player.isDetective()) return Optional.of(player.location());
				else if (player.isMrX()) {
					if (intCurrentRound == 0) return Optional.of(intPublicLastSeenPosition);
					if (publicRounds.get(intCurrentRound - 1) == true) {
						intPublicLastSeenPosition = player.location();
						return Optional.of(intPublicLastSeenPosition);
					} else return Optional.of(intPublicLastSeenPosition);
				}
			}
		}
		return Optional.of(null);
	}
	// Gets the amount of tickets of a certain type owned by a certain -player. Return empty if the player doesn't exist.
	@Override
	public Optional < Integer > getPlayerTickets(Colour colour, Ticket ticket) {
		boolean validPlayer = false;
		for (ScotlandYardPlayer player: scotlandYardPlayers) {
			if (colour == player.colour()) validPlayer = true;
		}
		if (validPlayer == false) return Optional.empty();
		for (ScotlandYardPlayer player: scotlandYardPlayers) {
			if (player.colour() == colour) return Optional.of(player.tickets().get(ticket));
		}
		return Optional.of(null);
	}

	// Returns true if the player of a specified colour can't move.
	// Compares available routes to the player's tickets.
	// Makes sure destination isn't occupied.

	private boolean isPlayerStuck(Colour colour) {
		// Unfortunately, this method may not be implemented using iteration since tickets do not directly map to transport methods.
		int loc = 0;
		if (colour != BLACK) loc = getPlayerLocation(colour).get();
		else loc = publicMrXPlayer.location();
		Collection < Edge < Integer, Transport >> edges = graphPublic.getEdgesFrom(graphPublic.getNode(loc));
		// Can they move via secret?
		if (colour == BLACK) {
			if (getPlayerTickets(colour, SECRET).get() != 0) {
				for (Edge < Integer, Transport > edge: edges) {
					if (nodeOccupied(edge.destination().value()) == false) return false;
				}
			}
		}
		// Can they move via taxi?
		if (getPlayerTickets(colour, TAXI).get() != 0) {
			for (Edge < Integer, Transport > edge: edges) {
				if (edge.data() == Transport.TAXI && nodeOccupied(edge.destination().value()) == false) return false;
			}
		}
		// Can they move via bus?
		if (getPlayerTickets(colour, BUS).get() != 0) {
			for (Edge < Integer, Transport > edge: edges) {
				if (edge.data() == Transport.BUS && nodeOccupied(edge.destination().value()) == false) return false;
			}
		}
		// Can they move via underground?
		if (getPlayerTickets(colour, UNDERGROUND).get() != 0) {
			for (Edge < Integer, Transport > edge: edges) {
				if (edge.data() == Transport.UNDERGROUND && nodeOccupied(edge.destination().value()) == false) return false;
			}
		}
		return true;
	}

	// Method determines whether a detective is currently sharing the same position as mrX.
	private boolean isMrXCaptured() {
		for (ScotlandYardPlayer player: scotlandYardPlayers) {
			if (player.isDetective() && player.location() == publicMrXPlayer.location()) return true;
		}
		return false;
	}

	// Our public method to check whether the game is over. Contains all game over tests for all conditions.
	@Override
	public boolean isGameOver() {
		// Are we out of rounds?
		// We should only check this property if we are testing it at the end of a rotation
		if (intCurrentRound == intMaxRounds && currentPlayer == publicMrXPlayer) {
			mrXWon = true;
			return true;
		}
		// Is Mr X Captured?
		if (isMrXCaptured()) return true;

		// Do any detectives have tickets remaining?
		boolean ticketsRemaining = false;
		for (ScotlandYardPlayer player: scotlandYardPlayers) {
			for (Ticket ticket: Ticket.values())
				if (getPlayerTickets(player.colour(), ticket).get() != 0 && player.isDetective()) ticketsRemaining = true;
		}
		if (!ticketsRemaining) {
			mrXWon = true;
			return true;
		}

		// Are all the detectives stuck?
		boolean detectivesStuck = true;
		for (Colour colour: detectiveColours) {
			if (!isPlayerStuck(colour)) detectivesStuck = false;
		}
		if (detectivesStuck) {
			mrXWon = true;
			// Do spectator shizzle for mrX winning:
			for (Spectator s: publicSpectators) {
				Set < Colour > mrXColour = new HashSet < > ();
				mrXColour.add(BLACK);
				s.onGameOver(this, mrXColour);
			}
			return true;
		}

		// Is Mr X stuck?
		if (isPlayerStuck(BLACK)) {
			mrXWon = false;
			return true;
		}
		return false;
	}


	@Override
	public Colour getCurrentPlayer() {
		return (currentPlayer.colour());
	}

	@Override
	public int getCurrentRound() {
		return intCurrentRound;
	}

	@Override
	public List < Boolean > getRounds() {
		return (Collections.unmodifiableList(publicRounds));

	}

	@Override
	public Graph < Integer, Transport > getGraph() {
		return new ImmutableGraph < Integer, Transport > (graphPublic);
	}

}