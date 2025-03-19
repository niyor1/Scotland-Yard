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

    public double computeMinMax(Node node) {
        if (node.children.isEmpty()) {
            return node.value;
        }

        double bestValue;

        if (node.findMax) {
            bestValue = Double.MIN_VALUE;
        }
        else{
            bestValue = Double.MAX_VALUE;
        }
        for (Node child : node.children) {
            double childValue = computeMinMax(child);
            if (node.findMax) {
                bestValue = Math.max(bestValue, childValue);
            } else {
                bestValue = Math.min(bestValue, childValue);
            }
        }

        node.value = bestValue;
        return node.value;
    }

}
