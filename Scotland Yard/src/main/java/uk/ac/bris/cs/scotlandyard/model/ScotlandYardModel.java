package uk.ac.bris.cs.scotlandyard.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import uk.ac.bris.cs.gamekit.graph.Graph;
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
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.scotlandyard.ui.controller.Debug;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {
	ArrayList<ScotlandYardPlayer> scotlandYardPlayers = new ArrayList<>();
	ArrayList<Colour> detectiveColours = new ArrayList<>();
	ArrayList<Colour> allColours = new ArrayList<>();
	final Graph<Integer, Transport> graphPublic;
	final List<Boolean> publicRounds;
	public int intCurrentRound = 0;
	ScotlandYardPlayer currentPlayer;




	void addcolours(){
		allColours.add(RED);
		allColours.add(BLUE);
		allColours.add(BLACK);
		allColours.add(BLUE);
		allColours.add(YELLOW);
		allColours.add(WHITE);
	}
	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
							 PlayerConfiguration... restOfTheDetectives) {
			publicRounds = rounds;
			graphPublic = graph;
			addcolours();
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
						throw new IllegalArgumentException("mrX is just gonna be caught you utter wibbly! He's got no tickets whatsoever");
					}
					System.out.print(player.tickets());
					if(!player.hasTickets(TAXI, 1) || !player.hasTickets(BUS, 2) || !player.hasTickets(UNDERGROUND, 3) || !player.hasTickets(SECRET, 5) || !player.hasTickets(DOUBLE, 4)){

						//throw new IllegalArgumentException("mrX has not been dealt the correct amount of cards. I call shenanigans here!");
					}
				}
				else if(player.isDetective()){
					if(!player.hasTickets(TAXI) ||!player.hasTickets(BUS) || !player.hasTickets(UNDERGROUND)){
						throw new IllegalArgumentException("mrX is just gonna get away, the detectives have no tickets!");
					}
				}
			}
			if(mrXCount > 1){
				throw new IllegalArgumentException("There's some sort of criminal ring going on, there's more than one mrX!");
			}

			//Do the player tests on Mr X:






		//Check that all detectives don't have any secret or double tickets


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
	public void startRotate() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
		// TODO

		List<Colour> getPlayerList = new ArrayList<>();
		for (ScotlandYardPlayer player : scotlandYardPlayers) {
			getPlayerList.add(player.colour());
		}

		final List<Colour> getPlayerListFinal = getPlayerList;
		//throw new RuntimeException("Implement me");
		return getPlayerListFinal;
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		// TODO
		Set<Colour> winningColours = new HashSet<>();
		if(isGameOver() == true){

			for (Colour colour : detectiveColours){
				winningColours.add(colour);
			}
		}
		return winningColours;

	}
	//Get a list of the colours that don't exist in the current game
	@Override
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
					return Optional.of(0);
				}
			}
		}
		return Optional.of(null);
	}

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

	@Override
	public boolean isGameOver() {
		// TODO
		return false;

	}

	@Override
	public Colour getCurrentPlayer() {
		// just gets mrx right now
		return (BLACK);
	}

	@Override
	public int getCurrentRound() {
		// TODO
		return intCurrentRound;


	}

	@Override
	public List<Boolean> getRounds() {
		// TODO
		return(publicRounds);

	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		// TODO
		final Graph<Integer, Transport> graphLocal = graphPublic;
		return graphLocal;

	}

}
