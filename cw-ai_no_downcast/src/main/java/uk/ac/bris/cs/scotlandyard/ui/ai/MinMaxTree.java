package uk.ac.bris.cs.scotlandyard.ui.ai;
import java.util.*;

//tree node
class Node {
    int value;
    List<Node> children;
    boolean findMax;

    public Node(int x, boolean findMax)
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

    public int computeMinMax(Node node) {
        if (node.children.isEmpty()) {
            return node.value;
        }

        int bestValue = 0;
        if (node.findMax) {
            bestValue = Integer.MIN_VALUE;
        }
        else{
            bestValue = Integer.MAX_VALUE;
        }
        for (Node child : node.children) {
            int childValue = computeMinMax(child);
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
