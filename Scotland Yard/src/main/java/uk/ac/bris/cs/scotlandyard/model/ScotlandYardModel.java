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
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
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



	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
							 PlayerConfiguration... restOfTheDetectives) {
		// TODO

			//this.rounds = requireNonNull(rounds);

			//Creates a list of all of our player configurations, lets us do some iteration.
			ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
			ArrayList<ScotlandYardPlayer> scotlandYardPlayers = new ArrayList<>();


			configurations.add(mrX);
			configurations.add(firstDetective);
			for (PlayerConfiguration configuration : restOfTheDetectives) {

				configurations.add(configuration);

			}

			validateConfigurations(mrX, rounds, graph, configurations);


			//adds mrX to our player list
			scotlandYardPlayers.add(new ScotlandYardPlayer(mrX.player,mrX.colour, mrX.location, mrX.tickets));
			//adds the first detective to our player list
			scotlandYardPlayers.add(new ScotlandYardPlayer(firstDetective.player,firstDetective.colour, firstDetective.location, firstDetective.tickets));
			//adds the rest of the detectives to our player list
			for (PlayerConfiguration detective : restOfTheDetectives) {
				scotlandYardPlayers.add(new ScotlandYardPlayer(detective.player,detective.colour, detective.location, detective.tickets));
			}

			validatePlayers(scotlandYardPlayers);
	}

	public void validateConfigurations(PlayerConfiguration mrX, List<Boolean> rounds, Graph<Integer, Transport> graph, List<PlayerConfiguration> configurations) {
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


	}


		public void validatePlayers(List<ScotlandYardPlayer> scotlandYardPlayerList){
			// We need to set up each player with their tickets
			//Tests the tickets owned by each detective
			for(ScotlandYardPlayer player : scotlandYardPlayerList){
				if(player.hasTickets(DOUBLE) && player.isDetective()){
					throw new IllegalArgumentException("Yer bastidge, yer bleedin worm! By joh! No detective should have a double ticket");
				}
				if(player.hasTickets(SECRET) && player.isDetective()){
					throw new IllegalArgumentException("The name's Bond... James Bond... but I shouldn't have a secret ticket");
				}
				System.out.print(player.tickets());
				if(player.isMrX()){
					if(!player.hasTickets(TAXI)){
						System.out.print("Mr X has no tickets");

					}

					if(!player.hasTickets(TAXI) ||!player.hasTickets(BUS) || !player.hasTickets(UNDERGROUND) || !player.hasTickets(SECRET) ){
						throw new IllegalArgumentException("mrX is just gonna be caught you utter wibbly! He's got no tickets whatsoever");
					}
				}
		}

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
		throw new RuntimeException("Implement me");
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public boolean isGameOver() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Colour getCurrentPlayer() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getCurrentRound() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Boolean> getRounds() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		// TODO
		throw new RuntimeException("Implement me");
	}

}
