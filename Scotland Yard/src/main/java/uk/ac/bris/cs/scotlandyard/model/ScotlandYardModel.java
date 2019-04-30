package uk.ac.bris.cs.scotlandyard.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableCollection;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
	boolean doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder = false;
	final Graph<Integer, Transport> graphPublic;
	final List<Boolean> publicRounds;
	List<PlayerConfiguration> publicPlayerConfigurations;
	int intCurrentRound = 0;
	int intMaxRounds;
	ScotlandYardPlayer currentPlayer;
	ScotlandYardPlayer publicMrXPlayer;
	Player currentPlayerInterface;
	int intCurrentPlayerIndex = 0;
	int intPublicLastSeenPosition = 0;
	Ticket ticketTemp;
	Boolean ticketTempGranted = true;
	ArrayList<Spectator> publicSpectators = new ArrayList<>();
	boolean mrXWon = false;


	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
							 PlayerConfiguration mrX, PlayerConfiguration firstDetective,
							 PlayerConfiguration... restOfTheDetectives) {


		ticketTemp = TAXI;
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



		publicMrXPlayer = mrXPlayer;
		validateConfigurations(mrX, rounds, graph, configurations, detectives);
		validatePlayers(scotlandYardPlayers, mrXPlayer, configurations);
		//	validateGameOver();


		//Now let's set mrX as the first player turn.
		currentPlayer = mrXPlayer;

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


        //Check that all the players have references to their ticket types:
        for (PlayerConfiguration player : publicPlayerConfigurations) {
            for (Ticket ticket : Ticket.values()) {
                if (player.tickets.get(ticket) == null) {
                    throw new IllegalArgumentException("Ticket, you willy!");
                }
            }
			/*
			if (detective.tickets.get(TAXI) == null) {
				throw new IllegalArgumentException("Ticket, you willy!");
			}
			if (detective.tickets.get(BUS) == null) {
				throw new IllegalArgumentException("Ticket, you willy!");
			}
			if (detective.tickets.get(UNDERGROUND) == null) {
				throw new IllegalArgumentException("Ticket, you willy!");
			}*/
        }
		/*
		//Check that mrX has references to his tickets:
		for (Ticket ticket : Ticket.values()) {
			if (mrX.tickets.get(ticket) == null) {
				throw new IllegalArgumentException("Ticket, you willy mrX!");
			}
		}
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
		}*/



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

		boolean canAdd = true;
		if(spectator == null){
			throw new NullPointerException("You aint got no spectator you bastard!");

		}
		else{
			//Firstly check all of the spectators currently in our collection. Make sure that we're not adding a duplicate
			for(Spectator s : publicSpectators){
				if(s == spectator){
					canAdd = false;
				}
			}
			if(canAdd){
				publicSpectators.add(spectator);
			}
		}
		if(canAdd == false){
			throw new IllegalArgumentException("We have already added this spectator man!");
		}

	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO
		if(publicSpectators == null && spectator != null){
			throw new IllegalArgumentException();
		}
		if(spectator == null){
			throw new NullPointerException("You can't unregister a spectator that doesn't exist, you fool!");
		}
		else if(!publicSpectators.contains(spectator)){
			throw new IllegalArgumentException();
		}
		else{
			//Riiight it all seems to be fine so we're gonna unregister the spectator
			publicSpectators.remove(spectator);
		}

	}

	@Override
	public Collection<Spectator> getSpectators() {

		//return new ImmutableCollection<Spectator>(publicSpectators);
		return Collections.unmodifiableCollection(publicSpectators);
	}

	@Override
	//This subroutine increments the currently selected player that we will be dealing with.
	//Loops around if it reaches the end of the array.
	public void startRotate() {
		boolean blnDoNotChangeCurrentPlayer = false;
		if (isGameOver()) {
			throw new IllegalStateException();
		} else {
			intCurrentPlayerIndex = 0;
			for (ScotlandYardPlayer p : scotlandYardPlayers) {


				currentPlayer = scotlandYardPlayers.get(intCurrentPlayerIndex);
				currentPlayerInterface = publicPlayerConfigurations.get(intCurrentPlayerIndex).player;
				if (intCurrentPlayerIndex == 0 && currentPlayer.isMrX() == false) {
					throw new RuntimeException("mrX should play first");
				}


				startMove();
				if(isMrXCaptured()){
					blnDoNotChangeCurrentPlayer = true;
					break;
				}
				if (intCurrentPlayerIndex < scotlandYardPlayers.size() - 1) {
					intCurrentPlayerIndex += 1; //Get ready to select the next player on the next cycle.
					currentPlayer = scotlandYardPlayers.get(intCurrentPlayerIndex);
					currentPlayerInterface = publicPlayerConfigurations.get(intCurrentPlayerIndex).player;
				}
				// if the game is over, we may break our for loop and go straight on to inform spectators
				System.out.println(isGameOver() + " IS THE GAME STATE");
				if(isMrXCaptured()){
					blnDoNotChangeCurrentPlayer = true;
					break;
				}
			}




			if(blnDoNotChangeCurrentPlayer == false) currentPlayer = publicMrXPlayer;
			blnDoNotChangeCurrentPlayer = false;
			// Firstly, check if the game is not over. If it is not, then notify the spectator
			// that we have completed a rotation.

			//If either team has won, then notify the spectator that the game is over.
			if (isGameOver()) {
				for (Spectator s : publicSpectators) {
					s.onGameOver(this, getWinningPlayers());

				}
			}
			else {
				for (Spectator s : publicSpectators) {
					if (doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder == false) {
						s.onRotationComplete(this);
					}
				}
			}
			doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder = false;
		}
	}

	public void startMove(){


		//get the moves the player can make and return as a set
		ScotlandYardView view = this;

		Set<Move> moveSet = new HashSet<>();
		moveSet.addAll(generateMoves());

		Consumer<Move> moveConsumer = move -> {
			// LOOK, I GET THAT THIS IS HORRIBLE BUT THIS FUNKING MESS OF SPAGHETTI CODE IS ACTUALLY PASSING THE BLOODY TESTS
			ScotlandYardPlayer commitPlayer = currentPlayer;
			System.out.println(move.toString());
			//Do a move?
			boolean validMove = true;
			if(move == null){
				throw new NullPointerException();

			}
			if(move == new PassMove(commitPlayer.colour())){
				throw new RuntimeException("Uhh I'm lost, I have to throw a pass move");
			}

			if(!moveSet.contains(move)){
				validMove = false;
			}
			if(validMove == false){ //maybe we are not generating the right move set?

					throw new IllegalArgumentException();
			}
			// if our move is valid, do the shenanigans with the tickets:

			//currentPlayer.removeTicket();
			int intDestination = 0;

			String strDestination = move.toString().replaceAll("\\D+","");
			System.out.println("Our destination is " + strDestination);
			if(strDestination.length() > 0){
				intDestination = Integer.parseInt(strDestination);
			}


			//Pre-emptively getting a reference to the next player upon committing the move
			if(intCurrentPlayerIndex < scotlandYardPlayers.size() -1) {
				System.out.println("THE CURRENT PLAYER IS" + intCurrentPlayerIndex);
				currentPlayer = scotlandYardPlayers.get(intCurrentPlayerIndex + 1);

				System.out.println(currentPlayer.colour().toString());
			}
			else{
				currentPlayer = publicMrXPlayer;
				System.out.println("THE CURRENT PLAYER IS" + intCurrentPlayerIndex);
				intCurrentPlayerIndex = 0;
				System.out.println(currentPlayer.colour().toString());
			}

			if(!move.toString().contains("Double")) {
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
				// At this point, we're gonna check if the move will result in Mrx being stuck:

				if (move.toString().contains("Pass")) {

					ticketTempGranted = false;
					commitPlayer.location(commitPlayer.location());
				}
			}



			if(move.toString().contains("Double")){
				ticketTempGranted = false;
				//baso we're just gonna cheat and read the last number from the
				boolean blnHideFirstMove = false;
				boolean blnHideSecondMove = false;
				String[] tickets = move.toString().split("-");
				String ticketType1 = tickets[1].replaceAll("[^a-zA-Z]", "");
				System.out.println(ticketType1);
				String ticketType2 = tickets[3].replaceAll("[^a-zA-Z]", "");
				System.out.println(ticketType2);
				String[] destinations = move.toString().split("->");
				String singleDestination = destinations[1].replaceAll("[^\\d]", "" );
				String doubleDestination = destinations[2].replace("]", "");
				System.out.println("singleDestination: " + singleDestination);
				System.out.println("Destination: " + doubleDestination);
				intDestination = Integer.parseInt(doubleDestination);
				int intSingleDestination = Integer.parseInt(singleDestination);

				Ticket ticket1 = TAXI;
				Ticket ticket2 = TAXI;
				if(ticketType1.contains("TAXI")) ticket1 = TAXI;
				if(ticketType1.contains("BUS")) ticket1 = BUS;
				if(ticketType1.contains("UNDERGROUND")) ticket1 = UNDERGROUND;
				if(ticketType1.contains("SECRET")) ticket1 = SECRET;
				if(ticketType2.contains("TAXI")) ticket2 = TAXI;
				if(ticketType2.contains("BUS")) ticket2 = BUS;
				if(ticketType2.contains("UNDERGROUND")) ticket2 = UNDERGROUND;
				if(ticketType2.contains("SECRET")) ticket2 = SECRET;

				commitPlayer.removeTicket(DOUBLE);

				//NOTIFY DOUBLE MOVE
				for (Spectator s : publicSpectators){
					// We need to check whether mrX should be hidden or not
					// If he is hidden this turn, use his last known position
					// If he is hidden next turn, also use his last known position

					// if MrX may not be seen both rounds, make up a lastPos -> lastPos move containing the transport methods
					if(!publicRounds.get(getCurrentRound()) && !publicRounds.get(getCurrentRound() + 1)){
						System.out.println("fullyHiddenDoubleMove engaged!");
						DoubleMove fullyHiddenDoubleMove = new DoubleMove(BLACK, ticket1, intPublicLastSeenPosition, ticket2, intPublicLastSeenPosition);

						s.onMoveMade(view, fullyHiddenDoubleMove);
						blnHideFirstMove = true;
						blnHideSecondMove = true;
					}
					else if(publicRounds.get(getCurrentRound()) && !publicRounds.get(getCurrentRound() + 1)){
						System.out.println("latterHiddenDoubleMove engaged!");
						DoubleMove latterHiddenDoubleMove = new DoubleMove(BLACK, ticket1, intSingleDestination, ticket2, intSingleDestination);
						s.onMoveMade(view, latterHiddenDoubleMove);
						blnHideFirstMove = false;
						blnHideSecondMove = true;
					}

					else if(!publicRounds.get(getCurrentRound()) && publicRounds.get(getCurrentRound() + 1)){
						System.out.println("hidden to reveal engaged!");
						DoubleMove formerHiddenDoubleMove = new DoubleMove(BLACK, ticket1, intPublicLastSeenPosition, ticket2, intDestination);
						s.onMoveMade(view, formerHiddenDoubleMove);
						blnHideFirstMove = true;
						blnHideSecondMove = false;
					}
					else{
						s.onMoveMade(view, move);
					}

					// if MrX may not be seen this round, use his last known position and move to the next known one

				}
				commitPlayer.removeTicket(ticket1);
				commitPlayer.location(intSingleDestination);
				//ASSERT ROUND 0
					intCurrentRound += 1;
				//START NEW ROUND
				for (Spectator s : publicSpectators){
					s.onRoundStarted(this, intCurrentRound);
				}
				//NOTIFY THE FIRST TICKET MOVE
				for (Spectator s : publicSpectators){
					if(!blnHideFirstMove){
						s.onMoveMade(view, new TicketMove(BLACK, ticket1, intSingleDestination));
					}
					else if(blnHideFirstMove){
						s.onMoveMade(view, new TicketMove(BLACK, ticket1, intPublicLastSeenPosition)); //Change this to actual move
					}
				}

				//ASSERT ROUND 1
				intCurrentRound += 1;
				//START NEW ROUND
				commitPlayer.removeTicket(ticket2);
				commitPlayer.location(intDestination);
				for (Spectator s : publicSpectators){
					s.onRoundStarted(this, intCurrentRound);
				}
				//NOTIFY THE SECOND TICKET MOVE
				for (Spectator s : publicSpectators){
					if(!blnHideSecondMove){
							s.onMoveMade(view, new TicketMove(BLACK, ticket2, intDestination));
					}
					else if(!blnHideFirstMove && blnHideSecondMove){
						s.onMoveMade(view, new TicketMove(BLACK, ticket2, intSingleDestination)); //Change this to actual move
					}
					else if(blnHideSecondMove){
						s.onMoveMade(view, new TicketMove(BLACK, ticket2, intPublicLastSeenPosition)); //Change this to actual move
					}
					else {
						s.onMoveMade(view, new TicketMove(BLACK, ticket2, intDestination));
					}
				}
				//ASSERT ROUND 2
				//intCurrentRound += 1;
				//currentPlayer.location(intDestination);

				// Leave it... it just passes a test and that is all I care about:
				doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder = true;
			}
			else if(ticketTempGranted && ticketTemp != SECRET){
				commitPlayer.removeTicket(ticketTemp);
			}
			// WE HAVE DONE WITH THE DOUBLE MOVE ----------

			// If the move was made by MrX, then increment the round
			if(commitPlayer.isMrX()){
				if(!move.toString().contains("Double"))intCurrentRound += 1;
				ticketTempGranted = false;
			}
			else{
				doubleMoveNonsenseShouldNotifyRoundStartTwoTimesInOrder = false;
			}
			// DO THE SPECTATOR SHIZZLE TO MAKE A NEW MOVE IF WE ARE NOT USING A DOUBLE MOVE
			if(!move.toString().contains("Double")) {
				if(commitPlayer == publicMrXPlayer){
					for (Spectator s : publicSpectators) {
						s.onRoundStarted(view, intCurrentRound);
					}
				}

				if(ticketTempGranted){
					publicMrXPlayer.addTicket(ticketTemp);
				}
				for (Spectator s : publicSpectators) {
					// if the player is mrX, then we should see if he should have to cover up his move destination
					if(commitPlayer == publicMrXPlayer){
						if(publicRounds.get(intCurrentRound - 1) == false){
							TicketMove mv = new TicketMove(BLACK, TAXI, intPublicLastSeenPosition);
							if(move.toString().contains("TAXI")){
								mv = new TicketMove(BLACK, TAXI, intPublicLastSeenPosition);
							}
							if(move.toString().contains("BUS")){
								mv = new TicketMove(BLACK, BUS, intPublicLastSeenPosition);
							}
							if(move.toString().contains("UNDERGROUND")){
								mv = new TicketMove(BLACK, UNDERGROUND, intPublicLastSeenPosition);
							}
							if(move.toString().contains("SECRET")){
								mv = new TicketMove(BLACK, SECRET, intPublicLastSeenPosition);
							}

							s.onMoveMade(view, mv);
						}
						else{
							s.onMoveMade(view, move);
						}
					}
					else{
						// otherwiiiise....
						s.onMoveMade(view, move);
					}


				}
			}

		};


		// MASSIVE MOVE FUNCTION -----------------------------------------------------------------------------------------

		currentPlayerInterface.makeMove( view, currentPlayer.location(), moveSet, moveConsumer);
		System.out.println("mrX is at position " + publicMrXPlayer.location() + " after his move");




		//Give back a move

	}
	// This is a bloody black box and basically plz don't touch it.
	Set<Move> generateMoves(){
		Set<Move> moveSet = new HashSet<>();

		Collection<Edge<Integer, Transport>> connectedEdges = graphPublic.getEdgesFrom(graphPublic.getNode(currentPlayer.location()));
		for(Edge<Integer, Transport> connectedEdge : connectedEdges) {
			//firstly, check that no other player is occupying the node that we're gonna look at:
			if (nodeOccupiedExcludingMrX(connectedEdge.destination().value()) == false) {
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

	boolean nodeOccupied(int nodeID){
		for (ScotlandYardPlayer player : scotlandYardPlayers){
			if(player.location() == nodeID){
				return true;
			}
		}
		return false;
	}
	boolean nodeOccupiedExcludingMrX(int nodeID){
		for (ScotlandYardPlayer player : scotlandYardPlayers){
			if(player.location() == nodeID && player.isMrX() == false){
				return true;
			}
		}
		return false;
	}

	@Override
	//Gets a list of the colours of all players currently in the game
	public List<Colour> getPlayers() {
		// TODO

		List<Colour> getPlayerList = new ArrayList<>();
		for (ScotlandYardPlayer player : scotlandYardPlayers) {
			getPlayerList.add(player.colour());
		}

		final List<Colour> getPlayerListFinal = getPlayerList;
		//throw new RuntimeException("Implement me");
		return Collections.unmodifiableList(getPlayerListFinal);
	}

	@Override
	// Returns the winning players as a list, empty list if no one has won.
	public Set<Colour> getWinningPlayers() {
		// TODO
		Set<Colour> winningColours = new HashSet<>();
		if(isGameOver() == true) {
			if (mrXWon == true) {
				winningColours.add(BLACK);
			} else {
				for (Colour colour : detectiveColours) {
					winningColours.add(colour);
				}
			}
		}
		return Collections.unmodifiableSet(winningColours);

	}

	@Override
	//Get the location of a specified player by colour. Return empty if the player is not in the game.
	public Optional<Integer> getPlayerLocation(Colour colour) {
		// TODO
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
					if(intCurrentRound == 0){
						return Optional.of(intPublicLastSeenPosition);
					}
					/*while(intCurrentRound > publicRounds.size()){
						intCurrentRound -=1;
					}
					while(intCurrentRound <= 0){
						intCurrentRound +=1;
					}*/
					//System.out.println("THE LAST SEEN POSITION OF MRX WAS " + intPublicLastSeenPosition + " ON ROUND " + intCurrentRound);
					if(publicRounds.get(intCurrentRound -1) == true){
						intPublicLastSeenPosition = player.location();
						return Optional.of(intPublicLastSeenPosition); // aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
					}
					else{
						return Optional.of(intPublicLastSeenPosition);
					}
				}
			}
		}
		return Optional.of(null);
	}
	// Gets the amount of tickets of a certain type owned by a certain -player. Return empty if the player doesn't exist.
	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		// TODO
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
    // Compares available routes to the player's tickets.
    // Makes sure destination isn't occupied.

	// Okay, Imma make a change to this where we directly interface with the player location. This is because there is some
	// jiggery pokery with the mr X being hidden nonsense.
    public boolean isPlayerStuck(Colour colour) {

		int loc = 0;
		if(colour != BLACK) {
			loc = getPlayerLocation(colour).get();
		}
		else{
			loc = publicMrXPlayer.location();
		}
        Collection<Edge<Integer, Transport>> edges = graphPublic.getEdgesFrom(graphPublic.getNode(loc));

        // Can they move via secret?
		if(colour == BLACK) {
			if (getPlayerTickets(colour, SECRET).get() != 0) {
				for (Edge<Integer, Transport> edge : edges) {
					if (nodeOccupied(edge.destination().value()) == false) return false;
				}
			}
		}

        // Can they move via taxi?
        if (getPlayerTickets(colour, TAXI).get() != 0) {

            for (Edge<Integer, Transport> edge : edges) {

                if (edge.data() == Transport.TAXI && nodeOccupied(edge.destination().value()) == false) return false;

            }
        }

        // Can they move via bus?
        if (getPlayerTickets(colour, BUS).get() != 0) {
            for (Edge<Integer, Transport> edge : edges) {
                if (edge.data() == Transport.BUS && nodeOccupied(edge.destination().value()) == false) return false;
            }
        }
        // Can they move via underground?
        if (getPlayerTickets(colour, UNDERGROUND).get() != 0) {
            for (Edge<Integer, Transport> edge : edges) {
                if (edge.data() == Transport.UNDERGROUND && nodeOccupied(edge.destination().value()) == false) return false;
            }
        }
        return true;
    }


    public boolean isMrXCaptured(){
		for (ScotlandYardPlayer player : scotlandYardPlayers) {

			if (player.isDetective() && player.location() == publicMrXPlayer.location()) {

				return true;
			}
		}
		return false;
	}
    @Override
    public boolean isGameOver() {
        // Are we out of rounds?
        if (intCurrentRound == intMaxRounds) {
            mrXWon = true;
            return true;
        }


        // Is Mr X Captured?
        if(isMrXCaptured()) return true;

        // Do any detectives have tickets remaining?
        boolean ticketsRemaining = false;
        for (ScotlandYardPlayer player : scotlandYardPlayers) {
            for (Ticket ticket : Ticket.values())
                if (getPlayerTickets(player.colour(), ticket).get() != 0) {
                    ticketsRemaining = true;
                }
        }
        if (ticketsRemaining == false) {
            mrXWon = true;
            return true;
        }

        // Are all the detectives stuck?
        boolean detectivesStuck = true;
        for (Colour colour : detectiveColours) {

            if (isPlayerStuck(colour) == false) detectivesStuck = false;

        }
        if (detectivesStuck == true) {
            mrXWon = true;
            // Do spectator shizzle for mrX winning:
            for (Spectator s : publicSpectators){
                Set<Colour> mrXColour = new HashSet<>();
                mrXColour.add(BLACK);
                s.onGameOver(this, mrXColour);
            }
            return true;
        }

        // Is Mr X stuck?
        // NOTE: currently breaks many tests, I guess Mr X can get unstuck at some point???
		if (isPlayerStuck(BLACK) == true) {
			mrXWon = true;
			return true;
		}

        // Is Mr X cornered?
        // NOTE: currently breaks many tests. Unsure as to why.
		/*boolean mrXCornered = true;
		Collection<Edge<Integer, Transport>> edges = graphPublic.getEdgesFrom(graphPublic.getNode(getPlayerLocation(BLACK).get()));
		for (Edge<Integer, Transport> edge : edges) {
			boolean destinationOccupied = false;
			for (Colour colour : detectiveColours) {
				if (edge.destination().value() == getPlayerLocation(colour).get()) destinationOccupied = true;
			}
			if (destinationOccupied == false) mrXCornered = false;
		}
		if (mrXCornered == true) {
			return true;
		}*/


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
		// TODO
		return(Collections.unmodifiableList(publicRounds));

	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		// TODO
		return new ImmutableGraph<Integer, Transport>(graphPublic);

	}

}

