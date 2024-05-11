package environment;

public class Grid {
    private int width;
    private int height;
    // Additional properties like terrain or obstacles can be added here

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Method to check if a position is within the grid boundaries
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x <= width && y >= 0 && y <= height;
    }

}
