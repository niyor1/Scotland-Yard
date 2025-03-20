package uk.ac.bris.cs.scotlandyard.ui.ai;
import java.util.*;

//tree node
class Node {
    Double value;
    List<Node> children;
    boolean findMax;

    public Node(double x, boolean findMax)
    {
        this.value = x;
        this.children = new ArrayList<Node>();
        this.findMax = findMax;
    }

    public void addNode(Node node){
        this.children.add(node);
    }
}


public class MinMaxTree {
    Node root;

    public MinMaxTree (Node root){
        this.root = root;
    }

    public double computeMinMax(Node node, double alpha, double beta) {
        if (node.children.isEmpty()) {
            return node.value;
        }

        if (node.findMax) {
            double bestVal = Double.MIN_VALUE;
            for (Node child : node.children) {
                double childValue = computeMinMax(child, alpha, beta);
                bestVal = Math.max(bestVal, childValue);
                alpha = Math.max(alpha, bestVal);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestVal;

        }
        else{
            double bestVal = Double.MAX_VALUE;
            for (Node child : node.children) {
                double childValue = computeMinMax(child, alpha, beta);
                bestVal = Math.min(bestVal, childValue);
                beta = Math.min(beta, bestVal);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestVal;

        }
    }

}
