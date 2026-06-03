import os
import pickle
import neat

# Global variable to hold the network in memory
_net = None

def init():
    """Initializes the NEAT network. Called once from Java."""
    global _net
    if _net is not None:
        return "Already Initialized"

    try:
        # Get the directory where this script is located
        current_dir = os.path.dirname(__file__)
        config_path = os.path.join(current_dir, "config-feedforward.txt")
        model_path = os.path.join(current_dir, "best_bird.pkl")

        # Load NEAT Config
        config = neat.config.Config(
            neat.DefaultGenome, neat.DefaultReproduction,
            neat.DefaultSpeciesSet, neat.DefaultStagnation,
            config_path
        )

        # Load the saved Genome (Pickle)
        with open(model_path, "rb") as f:
            winner_genome = pickle.load(f)

        # Create the FeedForward Network
        _net = neat.nn.FeedForwardNetwork.create(winner_genome, config)
        return "AI Initialized Successfully"
    except Exception as e:
        return f"Error: {str(e)}"

def get_action(bird_y, top_pipe_y, bottom_pipe_y):
    """
    Takes 3 parameters and returns True if the bird should jump.
    """
    global _net
    if _net is None:
        return False

    # Activate the neural network
    # Inputs: bird Y, dist to top pipe bottom, dist to bottom pipe top
    inputs = (bird_y, abs(bird_y - top_pipe_y), abs(bird_y - bottom_pipe_y))
    output = _net.activate(inputs)

    # Return True if output > 0.5 (Standard NEAT activation threshold)
    return output[0] > 0.5
