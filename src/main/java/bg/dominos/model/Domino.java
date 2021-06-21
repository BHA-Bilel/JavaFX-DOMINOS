package bg.dominos.model;

import java.util.ArrayList;
import java.util.List;

public class Domino {

    private Domino right;
    private Domino left;
    private int leftValue;
    private int rightValue;
    private Position position;

    public Domino(int left, int right) {
        this.leftValue = left;
        this.rightValue = right;
    }

    public Domino(int left, int right, Position position) {
        this.leftValue = left;
        this.rightValue = right;
        this.position = position;
    }

    public Domino(Position position) {
        this.leftValue = -1;
        this.rightValue = -1;
        this.position = position;
    }

    public void Switch() {
        int x = rightValue;
        rightValue = leftValue;
        leftValue = x;
    }

    public void setRight(Domino right) {
        this.right = right;
    }

    public void setLeft(Domino left) {
        this.left = left;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isDouble() {
        return leftValue == rightValue;
    }

    public int getRightValue() {
        return rightValue;
    }

    public int getLeftValue() {
        return leftValue;
    }

    public Domino getRight() {
        return right;
    }

    public Domino getLeft() {
        return left;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (this.getClass() != obj.getClass())
            return false;
        Domino other = (Domino) obj;
        return (other.leftValue == leftValue && other.rightValue == rightValue)
                || (other.leftValue == rightValue && other.rightValue == leftValue);
    }

    /**
     * to send drawn domino from server
     */
    public Integer[] to_array() {
        return new Integer[]{leftValue, rightValue};
    }

    /**
     * for server to send new game dominoes in adt_data
     */
    public static Integer[] to_array(List<Domino> list) {
        Integer[] array = new Integer[list.size() * 2];
        int i = 0;
        for (Domino domino : list) {
            array[i++] = domino.getLeftValue();
            array[i++] = domino.getRightValue();
        }
        return array;
    }

    /**
     * for clients to get new game dominoes from game start adt_data
     */
    public static List<Domino> to_list(Integer[] array) {
        List<Domino> list = new ArrayList<>();
        int i = 0;
        while (i < array.length) list.add(new Domino(array[i++], array[i++], Position.BOTTOM));
        return list;
    }

}
