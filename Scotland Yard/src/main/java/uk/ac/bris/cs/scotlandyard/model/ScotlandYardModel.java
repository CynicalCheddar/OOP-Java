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
import static uk.ac.bris.cs.scotlandyard.model.Ticket.DOUBLE;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.SECRET;
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

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {



	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
							 PlayerConfiguration... restOfTheDetectives) {
		// TODO

			//this.rounds = requireNonNull(rounds);

			//Creates a list of all of our player configurations, lets us do some iteration.
			ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
			configurations.add(mrX);
			configurations.add(firstDetective);
			for (PlayerConfiguration configuration : restOfTheDetectives) {
				configurations.add(configuration);
			}

			validateState(mrX, rounds, graph, configurations);
	}

	public void validateState(PlayerConfiguration mrX, List<Boolean> rounds, Graph<Integer, Transport> graph, List<PlayerConfiguration> configurations){
		if(mrX.colour != BLACK || mrX.colour.isDetective()){
			throw new IllegalArgumentException("MrX should be Black... racial profiling");
		}
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("You aint got no rounds mate");
		}
		if(graph.isEmpty() == true){
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
