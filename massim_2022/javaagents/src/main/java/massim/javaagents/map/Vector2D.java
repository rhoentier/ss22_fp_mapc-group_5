package massim.javaagents.map;


import java.util.Objects;

public class Vector2D {

    public int x;
    public int y;

    public Vector2D() { }

    public Vector2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D v) {
        set(v);
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    public void setZero() {
        x = 0;
        y = 0;
    }

    public int[] getComponents() {
        return new int[]{x, y};
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public double getLengthSq() {
        return (x * x + y * y);
    }

    public double distanceSq(int vx, int vy) {
        vx -= x;
        vy -= y;
        return (vx * vx + vy * vy);
    }

    public double distanceSq(Vector2D v) {
        int vx = v.x - this.x;
        int vy = v.y - this.y;
        return (vx * vx + vy * vy);
    }

    public double distance(int vx, int vy) {
        vx -= x;
        vy -= y;
        return Math.sqrt(vx * vx + vy * vy);
    }

    public double distance(Vector2D v) {
        int vx = v.x - this.x;
        int vy = v.y - this.y;
        return Math.sqrt(vx * vx + vy * vy);
    }

    public double getAngle() {
        return Math.atan2(y, x);
    }

    // Methoden bzgl. Normalize auskommentiert, da mit int nicht unterstützt

    /*
    public void normalize() {
        double magnitude = getLength();
        x /= magnitude;
        y /= magnitude;
    }

    public Vector2D getNormalized() {
        double magnitude = getLength();
        return new Vector2D(x / magnitude, y / magnitude);
    }


    public static Vector2D toCartesian(double magnitude, double angle) {
        return new Vector2D(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
    }
    */

    public void add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
    }

    public void add(int vx, int vy) {
        this.x += vx;
        this.y += vy;
    }

    public static Vector2D add(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.x + v2.x, v1.y + v2.y);
    }

    public Vector2D getAdded(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D getAdded(int vx, int vy) {
        return new Vector2D(this.x += vx, this.y += vy);
    }

    public void subtract(Vector2D v) {
        this.x -= v.x;
        this.y -= v.y;
    }

    public void subtract(int vx, int vy) {
        this.x -= vx;
        this.y -= vy;
    }

    public static Vector2D subtract(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.x - v2.x, v1.y - v2.y);
    }

    public Vector2D getSubtracted(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    public void multiply(Vector2D v) {
        x *= v.x;
        y *= v.y;
    }

    public Vector2D getMultiplied(Vector2D v) {
        return new Vector2D(x * v.x, y * v.y);
    }

    public void divide(int scalar) {
        x /= scalar;
        y /= scalar;
    }

    public Vector2D getDivided(int scalar) {
        return new Vector2D(x / scalar, y / scalar);
    }

    public Vector2D getPerpendicular() {
        return new Vector2D(-y, x);
    }

    public int dot(Vector2D v) {
        return (this.x * v.x + this.y * v.y);
    }

    public int dot(int vx, int vy) {
        return (this.x * vx + this.y * vy);
    }

    public static int dot(Vector2D v1, Vector2D v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    public int cross(Vector2D v) {
        return (this.x * v.y - this.y * v.x);
    }

    public int cross(int vx, int vy) {
        return (this.x * vy - this.y * vx);
    }

    public static int cross(Vector2D v1, Vector2D v2) {
        return (v1.x * v2.y - v1.y * v2.x);
    }

    public double project(Vector2D v) {
        return (this.dot(v) / this.getLength());
    }

    public double project(int vx, int vy) {
        return (this.dot(vx, vy) / this.getLength());
    }

    public static double project(Vector2D v1, Vector2D v2) {
        return (dot(v1, v2) / v1.getLength());
    }

    // Methoden bzgl. Rotate auskommentiert, da mit int nicht unterstützt
    // Methoden könnten umgeschrieben werden auf cw / ccw in 90 Grad Schritten

    public void swap() {
        int tmp = x;
        x = y;
        y = tmp;
    }

    public Vector2D getSwapped() {
        return new Vector2D(y, x);
    }
    public void rotateCW() {
        swap();
        multiply(new Vector2D(-1, 1));
    }

    public void rotateCCW() {
        swap();
        multiply(new Vector2D(1, -1));
    }

    public Vector2D getRotatedCW() {
        return new Vector2D(this.getSwapped().getMultiplied(new Vector2D(-1, 1)));
    }

    public Vector2D getRotatedCCW() {
        return new Vector2D(this.getSwapped().getMultiplied(new Vector2D(1, -1)));
    }

    public void reverse() {
        x = -x;
        y = -y;
    }

    public Vector2D getReversed() {
        return new Vector2D(-x, -y);
    }

    public void mod(Vector2D range) {
        x += range.x;
        y += range.y;

        x %= range.x;
        y %= range.y;
    }
    @Override
    public Vector2D clone() {
        return new Vector2D(x, y);
    }

    @Override
    public String toString() {
        return "Vector2d[" + x + ", " + y + "]";
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.x;
        hash = 37 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;

        Vector2D v2 = (Vector2D) o;

        return Objects.equals(x, v2.x) && Objects.equals(y, v2.y);
    }
}

