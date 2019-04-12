package uk.ac.bris.cs.scotlandyard.model;

import java.util.*;

import uk.ac.bris.cs.gamekit.graph.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.*;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.ui.controller.Debug;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {
	ArrayList<ScotlandYardPlayer> scotlandYardPlayers = new ArrayList<>();
	ArrayList<Colour> detectiveColours = new ArrayList<>();

	final Graph<Integer, Transport> graphPublic;
	final List<Boolean> publicRounds;
	List<PlayerConfiguration> publicPlayerConfigurations;
	int intCurrentRound = 0;
	int intMaxRounds;
	ScotlandYardPlayer currentPlayer;
	Player currentPlayerInterface;
	int intCurrentPlayerIndex = 0;





	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
							 PlayerConfiguration... restOfTheDetectives) {


			intMaxRounds = rounds.size();
			publicRounds = rounds;
			graphPublic = graph;
			//Creates a list of all of our player configurations, lets us do some iteration.
			ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
			ArrayList<PlayerConfiguration> detectives = new ArrayList<>();
			configurations.add(mrX);
			configurations.add(firstDetective);
			detectives.add(firstDetective);
			detectiveColours.add(firstDetective.colour);
			for (PlayerConfiguration configuration : restOfTheDetectives) {

				configurations.add(configuration);
				detectives.add(configuration);
				detectiveColours.add(configuration.colour);
			}
			//just making this a global variable because that passes the tests. I don't like it but it works.
			publicPlayerConfigurations = configurations;

			//Creates a list of all our player objects:

			//adds mrX to our player list
			ScotlandYardPlayer mrXPlayer = new ScotlandYardPlayer(mrX.player,mrX.colour, mrX.location, mrX.tickets);
			scotlandYardPlayers.add(mrXPlayer);

			//adds the first detective to our player list
			scotlandYardPlayers.add(new ScotlandYardPlayer(firstDetective.player,firstDetective.colour, firstDetective.location, firstDetective.tickets));
			//adds the rest of the detectives to our player list
			for (PlayerConfiguration detective : restOfTheDetectives) {
				scotlandYardPlayers.add(new ScotlandYardPlayer(detective.player,detective.colour, detective.location, detective.tickets));
			}




			validateConfigurations(mrX, rounds, graph, configurations, detectives);
			validatePlayers(scotlandYardPlayers, mrXPlayer, configurations);
		//	validateGameOver();


		//Now let's set mrX as the first player turn.
		currentPlayer = mrXPlayer;

	}


	public void validateGameOver(){
		if(isGameOver() == true){
			throw new IllegalArgumentException("Fookin ell the game's already over!");
		}
	}

	public void validateConfigurations(PlayerConfiguration mrX, List<Boolean> rounds, Graph<Integer, Transport> graph, List<PlayerConfiguration> configurations, List<PlayerConfiguration> detectives) {
		if (mrX.colour != BLACK || mrX.colour.isDetective()) {
			throw new IllegalArgumentException("MrX should be Black... racial profiling");
		}
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("You aint got no rounds mate");
		}
		if (graph.isEmpty() == true) {
			throw new IllegalArgumentException("You bloody fruitshop owner has no graphs!");
		}

		//We need to pass a list of all of the player configurations
		// This should check that nobody is in the same location.
		Set<Integer> set = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (set.contains(configuration.location))
				throw new IllegalArgumentException("Duplicate location, you plonker!");
			set.add(configuration.location);
		}
		// This should check that nobody is of the same colour:
		Set<Colour> setColour = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (set.contains(configuration.colour))
				throw new IllegalArgumentException("Duplicate colour, you willy!");
			setColour.add(configuration.colour);
		}


		//Check that all the detectives have references to their ticket types:
		for (PlayerConfiguration detective : detectives) {
			if (detective.tickets.get(TAXI) == null) {
				throw new IllegalArgumentException("Ticket, you willy!");
			}
			if (detective.tickets.get(BUS) == null) {
				throw new IllegalArgumentException("Ticket, you willy!");
			}
			if (detective.tickets.get(UNDERGROUND) == null) {
				throw new IllegalArgumentException("Ticket, you willy!");
			}
		}
		//Check that mrX has references to his tickets:
		if (mrX.tickets.get(TAXI) == null) {
			throw new IllegalArgumentException("Ticket, you willy mrX!");
		}
		if (mrX.tickets.get(BUS) == null) {
			throw new IllegalArgumentException("Ticket, you willy mrX!");
		}
		if (mrX.tickets.get(UNDERGROUND) == null) {
			throw new IllegalArgumentException("Ticket, you willy mrX!");
		}
		if (mrX.tickets.get(SECRET) == null) {
			throw new IllegalArgumentException("Ticket, you willy mrX!");
		}
		if (mrX.tickets.get(DOUBLE) == null) {
			throw new IllegalArgumentException("Ticket, you willy mrX!");
		}


	}


		public void validatePlayers(List<ScotlandYardPlayer> scotlandYardPlayerList, ScotlandYardPlayer mrXPlayer, List<PlayerConfiguration> playerConfigurations){
			// We need to set up each player with their tickets
			//Tests the tickets owned by each detective
			int mrXCount = 0;
			for(ScotlandYardPlayer player : scotlandYardPlayerList){
				if(player.hasTickets(DOUBLE) && player.isDetective()){
					throw new IllegalArgumentException("Yer bastidge, yer bleedin worm! By joh! No detective should have a double ticket");
				}
				if(player.hasTickets(SECRET) && player.isDetective()){
					throw new IllegalArgumentException("The name's Bond... James Bond... but I shouldn't have a secret ticket");
				}

				if(player.isMrX()){
					mrXCount += 1;

					if(!player.hasTickets(TAXI) ||!player.hasTickets(BUS) || !player.hasTickets(UNDERGROUND) || !player.hasTickets(SECRET) || !player.hasTickets(DOUBLE)){
						//throw new IllegalArgumentException("mrX is just gonna be caught you utter wibbly! He's got no tickets whatsoever");
					}
					System.out.print(player.tickets());
					if(!player.hasTickets(TAXI, 1) || !player.hasTickets(BUS, 2) || !player.hasTickets(UNDERGROUND, 3) || !player.hasTickets(SECRET, 5) || !player.hasTickets(DOUBLE, 4)){

						//throw new IllegalArgumentException("mrX has not been dealt the correct amount of cards. I call shenanigans here!");
					}
				}
				else if(player.isDetective()){
					if(!player.hasTickets(TAXI) ||!player.hasTickets(BUS) || !player.hasTickets(UNDERGROUND)){
					//	throw new IllegalArgumentException("mrX is just gonna get away, the detectives have no tickets!");
					}
				}
			}
			if(mrXCount > 1){
				throw new IllegalArgumentException("There's some sort of criminal ring going on, there's more than one mrX!");
			}
	}

	@Override
	public void registerSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	//This subroutine increments the currently selected player that we will be dealing with.
	//Loops around if it reaches the end of the array.
	public void startRotate() {
		for(ScotlandYardPlayer p : scotlandYardPlayers) {
			currentPlayer = scotlandYardPlayers.get(intCurrentPlayerIndex);
			currentPlayerInterface = publicPlayerConfigurations.get(intCurrentPlayerIndex).player;
			if (intCurrentPlayerIndex == 0 && currentPlayer.isMrX() == false) {
				throw new RuntimeException("mrX should play first");
			}
			if (intCurrentPlayerIndex > publicPlayerConfigurations.size() - 1) {
				intCurrentPlayerIndex = 0;
			}
			startMove();
			// If the player is not responding:

			// Iterate through the players and let them make their moves:
			if (intCurrentPlayerIndex < scotlandYardPlayers.size() - 1) {
				intCurrentPlayerIndex += 1; //Get ready to select the next player on the next cycle.
				currentPlayer = scotlandYardPlayers.get(intCurrentPlayerIndex);
				currentPlayerInterface = publicPlayerConfigurations.get(intCurrentPlayerIndex).player;
			}
		}

			intCurrentPlayerIndex = 0;

			System.out.println(intCurrentRound);
			System.out.println(intCurrentPlayerIndex);

	}

	public void startMove(){
		/**
		 * Called when the player is required to make a move as required by
		 * the @link ScotlandYardGame}
		 *
		 * @param view a view of the current {@link ScotlandYardGame}, there are no
		 *        guarantees on immutability or thread safety so you should no hold
		 *        reference to the view beyond the scope of this method; never null
		 * @param location the location of the player
		 * @param moves valid moves the player can make; never empty and never null
		 * @param callback callback when a move is chosen from the given valid
		 *        moves, the game cannot
		 */
		//void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback);

		//get the moves the player can make and return as a set
		ScotlandYardView view = this;

		Set<Move> moveSet = new HashSet<>();
		moveSet.addAll(generateMoves());

		Consumer<Move> moveConsumer = move -> {
			//Do a move?
			boolean validMove = false;
			if(move == null){
				throw new NullPointerException();

			}
			if(move == new PassMove(currentPlayer.colour())){
				throw new RuntimeException("Uhh I'm lost");
			}
			for(Move mv : moveSet){
				if(mv == move){
					validMove = true;
					System.out.println("_");
					System.out.println("Our move is valid!");
				}
			}
			if(moveSet.size() == 0){
				validMove = true;
			}
			if(validMove == false){ //maybe we are not generating the right move set?
				System.out.println("_");
				System.out.println("The moveset is:" + moveSet);
			//	throw new IllegalArgumentException();
			}
			if(currentPlayer.isMrX()){
				intCurrentRound += 1;
			}
		};



			currentPlayerInterface.makeMove( view, currentPlayer.location(), moveSet, moveConsumer);


		//Give back a move

	}

	Set<Move> generateMoves(){
		Set<Move> moveSet = new HashSet<>();

		Collection<Edge<Integer, Transport>> connectedEdges = graphPublic.getEdgesFrom(graphPublic.getNode(currentPlayer.location()));
		for(Edge<Integer, Transport> connectedEdge : connectedEdges) {
			//firstly, check that no other player is occupying the node that we're gonna look at:
			if (nodeOccupied(connectedEdge.destination().value()) == false) {
				// add a preliminary secret move if the player is mrX:
				if (currentPlayer.isMrX() && currentPlayer.hasTickets(SECRET)) {
					moveSet.add(new TicketMove(currentPlayer.colour(), SECRET, connectedEdge.destination().value()));
					if (doubleMoveConditions()) {
						moveSet.addAll(generateDoubleMoves(connectedEdge.destination().value(), (new TicketMove(currentPlayer.colour(), SECRET, connectedEdge.destination().value())))); // passing our current edge destination and our root move.
					}
				}
				// if the edge is a taxi link
				if (connectedEdge.data() == Transport.TAXI && currentPlayer.hasTickets(TAXI)) {
					moveSet.add(new TicketMove(currentPlayer.colour(), TAXI, connectedEdge.destination().value()));
					if (doubleMoveConditions()) {
						moveSet.addAll(generateDoubleMoves(connectedEdge.destination().value(), (new TicketMove(currentPlayer.colour(), TAXI, connectedEdge.destination().value())))); // passing our current edge destination and our root move.
					}
				}
				// if the edge is a bus link
				if (connectedEdge.data() == Transport.BUS && currentPlayer.hasTickets(BUS)) {
					moveSet.add(new TicketMove(currentPlayer.colour(), BUS, connectedEdge.destination().value()));
					if (doubleMoveConditions()) {
						moveSet.addAll(generateDoubleMoves(connectedEdge.destination().value(), (new TicketMove(currentPlayer.colour(), BUS, connectedEdge.destination().value())))); // passing our current edge destination and our root move.
					}
				}
				// if the edge is a train
				if (connectedEdge.data() == Transport.UNDERGROUND && currentPlayer.hasTickets(UNDERGROUND)) {
					moveSet.add(new TicketMove(currentPlayer.colour(), UNDERGROUND, connectedEdge.destination().value()));
					if (doubleMoveConditions()) {
						moveSet.addAll(generateDoubleMoves(connectedEdge.destination().value(), (new TicketMove(currentPlayer.colour(), UNDERGROUND, connectedEdge.destination().value())))); // passing our current edge destination and our root move.
					}
				}
			}
		}
		System.out.print(moveSet);
		System.out.println(moveSet.size());
		System.out.println(moveSet.size());
		if(moveSet.size() == 0){
			//if there are no moves that can be made, produce a pass move
			moveSet.add(new PassMove(currentPlayer.colour()));
		}
		return moveSet;
	}
	Boolean doubleMoveConditions(){
		if(currentPlayer.isMrX() == true && (intCurrentRound < intMaxRounds - 2) && currentPlayer.hasTickets(DOUBLE)){
			return true;
		}
		else{
			return false;
		}
	}
	Set<Move> generateDoubleMoves(int nodeID, TicketMove rootMove){
		Set<Move> doubleMoveSet = new HashSet<>();
		TicketMove newMove;
		DoubleMove doubleMove;
		//take the ticket from a player, then give it back at the end:
		currentPlayer.removeTicket(rootMove.ticket());
		//get all the possible moves from the current location
		Collection<Edge<Integer, Transport>> connectedEdges = graphPublic.getEdgesFrom(graphPublic.getNode(nodeID));
		for(Edge<Integer, Transport> connectedEdge : connectedEdges) {
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
		currentPlayer.addTicket(rootMove.ticket());
		return doubleMoveSet;
	}

	Boolean nodeOccupied(int nodeID){
		for (ScotlandYardPlayer player : scotlandYardPlayers){
			if(player.location() == nodeID){
				return true;
			}
		}
		return false;
	}
	Boolean nodeOccupiedExcludingMrX(int nodeID){
		for (ScotlandYardPlayer player : scotlandYardPlayers){
			if(player.location() == nodeID && player.isMrX() == false){
				return true;
			}
		}
		return false;
	}
	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	//Gets a list of the colours of all players currently in the game
	public List<Colour> getPlayers() {
		List<Colour> getPlayerList = new ArrayList<>();
		for (ScotlandYardPlayer player : scotlandYardPlayers) {
			getPlayerList.add(player.colour());
		}

		final List<Colour> getPlayerListFinal = getPlayerList;
		//throw new RuntimeException("Implement me");
		return Collections.unmodifiableList(getPlayerListFinal);
	}

	@Override
	// Right now this function just says that the detectives win if the game is over.
	public Set<Colour> getWinningPlayers() {
		Set<Colour> winningColours = new HashSet<>();
		if(isGameOver() == true){

			for (Colour colour : detectiveColours){
				winningColours.add(colour);
			}
		}
		return Collections.unmodifiableSet(winningColours);

	}

	@Override
	//Get the location of a specified player by colour. Return empty if the player is not in the game.
	public Optional<Integer> getPlayerLocation(Colour colour) {
		boolean validPlayer = false;
		for (ScotlandYardPlayer player : scotlandYardPlayers){
			if(colour == player.colour()){
				validPlayer = true;
			}
		}
		if(validPlayer == false){
			return Optional.empty();
		}


		for (ScotlandYardPlayer player : scotlandYardPlayers) {
			if(player.colour() == colour){
				if(player.isDetective()){
					return Optional.of(player.location());
				}
				else if(player.isMrX()){
					return Optional.of(0);
				}
			}
		}
		return Optional.of(null);
	}
	// Gets the amount of tickets of a certain type owned by a certain -player. Return empty if the player doesn't exist.
	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		boolean validPlayer = false;
		for (ScotlandYardPlayer player : scotlandYardPlayers){
			if(colour == player.colour()){
				validPlayer = true;
			}
		}
		if(validPlayer == false){
			return Optional.empty();
		}

		for (ScotlandYardPlayer player : scotlandYardPlayers) {
			if(player.colour() == colour){
				return Optional.of(player.tickets().get(ticket));
			}
		}

		return Optional.of(null);
	}

	// Returns true if the player of a specified colour can't move.
	public boolean isPlayerStuck(Colour colour) {
		Collection<Edge<Integer, Transport>> edges = graphPublic.getEdgesFrom(graphPublic.getNode(getPlayerLocation(colour).get()));
		if (getPlayerTickets(colour, TAXI).get() != 0) return false;
		else if (getPlayerTickets(colour, BUS).get() != 0) {
			for (Edge<Integer, Transport> edge : edges) {
				if (edge.data() == Transport.BUS) return false;
			}
		}
		else if (getPlayerTickets(colour, UNDERGROUND).get() != 0) {
			for (Edge<Integer, Transport> edge : edges) {
				if (edge.data() == Transport.UNDERGROUND) return false;
			}
		}
		return true;
	}

	@Override
	public boolean isGameOver() {
		// Game can't be over on round 0.
		if (intCurrentRound == 0) return false;

		// Does a detective share a space with Mr X?
		for (Colour colour : detectiveColours) {
			if (getPlayerLocation(colour) == getPlayerLocation(BLACK)) {
				return true;
			}
		}

		// Do any detectives have tickets remaining?
		boolean ticketsRemaining = false;
		for (Colour colour : detectiveColours) {
			for (Ticket ticket : Ticket.values())
			if (getPlayerTickets(colour, ticket).get() != 0) {
				ticketsRemaining = true;
			}
		}
		if (ticketsRemaining == false) return true;

		// Are all the detectives stuck?
		boolean detectivesStuck = true;
		for (Colour colour : detectiveColours) {
			if (isPlayerStuck(colour) == false) detectivesStuck = false;
		}
		if (detectivesStuck == true) return true;

		// Is Mr X stuck?
		if (isPlayerStuck(BLACK) == true) return true;

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
	public List<Boolean> getRounds() {
		return(Collections.unmodifiableList(publicRounds));

	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		return new ImmutableGraph<Integer, Transport>(graphPublic);
	}

}
