package bg.dominos.model;

public class Domino {
	// LOGIC
	private Domino right;
	private Domino left;
	private int leftValue;
	private int rightValue;
	private Position position;

	public Domino(int left, int right, Position position) {
		this.leftValue = left;
		this.rightValue = right;
		this.position = position;
	}

	@Override
	public String toString() {
		return "[" + getLeftValue() + " | " + getRightValue() + "]";
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

	public boolean isDouble() {
		return leftValue == rightValue;
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

	// GETTERS SETTERS

	public int getRightValue() {
		return rightValue;
	}

	public int getLeftValue() {
		return leftValue;
	}

	public Domino getRight() {
		return right;
	}

	public void setRight(Domino right) {
		this.right = right;
	}

	public Domino getLeft() {
		return left;
	}

	public void setLeft(Domino left) {
		this.left = left;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
