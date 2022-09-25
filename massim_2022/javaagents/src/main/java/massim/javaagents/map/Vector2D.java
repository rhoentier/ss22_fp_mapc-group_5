package massim.javaagents.map;

import massim.javaagents.agents.Agent;
import massim.javaagents.agents.NextAgent;

import java.util.HashSet;
import java.util.Objects;

public class Vector2D {

    public int x;
    public int y;

    public Vector2D() {
    }

    public Vector2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D v) {
        Set(v);
    }

    public void Set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void Set(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    public void SetZero() {
        x = 0;
        y = 0;
    }

    public int[] GetComponents() {
        return new int[]{x, y};
    }

    public double GetLength() {
        return Math.sqrt(x * x + y * y);
    }

    public double GetLengthSq() {
        return (x * x + y * y);
    }

    public double DistanceSq(int vx, int vy) {
        vx -= x;
        vy -= y;
        return (vx * vx + vy * vy);
    }

    public double DistanceSq(Vector2D v) {
        int vx = v.x - this.x;
        int vy = v.y - this.y;
        return (vx * vx + vy * vy);
    }

    public double Distance(int vx, int vy) {
        vx -= x;
        vy -= y;
        return Math.sqrt(vx * vx + vy * vy);
    }

    public double Distance(Vector2D v) {
        int vx = v.x - this.x;
        int vy = v.y - this.y;
        return Math.sqrt(vx * vx + vy * vy);
    }

    public double GetAngle() {
        return Math.atan2(y, x);
    }

    // Methoden bzgl. Normalize auskommentiert, da mit int nicht unterstützt

    /*
    public void normalize() {
        double magnitude = GetLength();
        x /= magnitude;
        y /= magnitude;
    }

    public Vector2D getNormalized() {
        double magnitude = GetLength();
        return new Vector2D(x / magnitude, y / magnitude);
    }


    public static Vector2D toCartesian(double magnitude, double angle) {
        return new Vector2D(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
    }
     */
    public void Add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
    }

    public void Add(int vx, int vy) {
        this.x += vx;
        this.y += vy;
    }

    public static Vector2D Add(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.x + v2.x, v1.y + v2.y);
    }

    public Vector2D GetAdded(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D GetAdded(int vx, int vy) {
        return new Vector2D(this.x += vx, this.y += vy);
    }

    public void Subtract(Vector2D v) {
        this.x -= v.x;
        this.y -= v.y;
    }

    public void Subtract(int vx, int vy) {
        this.x -= vx;
        this.y -= vy;
    }

    public static Vector2D Subtract(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.x - v2.x, v1.y - v2.y);
    }

    public Vector2D GetSubtracted(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    public void Multiply(Vector2D v) {
        x *= v.x;
        y *= v.y;
    }

    public Vector2D GetMultiplied(Vector2D v) {
        return new Vector2D(x * v.x, y * v.y);
    }

    public void Divide(int scalar) {
        x /= scalar;
        y /= scalar;
    }

    public Vector2D GetDivided(int scalar) {
        return new Vector2D(x / scalar, y / scalar);
    }

    public Vector2D GetPerpendicular() {
        return new Vector2D(-y, x);
    }

    public int Dot(Vector2D v) {
        return (this.x * v.x + this.y * v.y);
    }

    public int Dot(int vx, int vy) {
        return (this.x * vx + this.y * vy);
    }

    public static int Dot(Vector2D v1, Vector2D v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    public int Cross(Vector2D v) {
        return (this.x * v.y - this.y * v.x);
    }

    public int Cross(int vx, int vy) {
        return (this.x * vy - this.y * vx);
    }

    public static int Cross(Vector2D v1, Vector2D v2) {
        return (v1.x * v2.y - v1.y * v2.x);
    }

    public double Project(Vector2D v) {
        return (this.Dot(v) / this.GetLength());
    }

    public double Project(int vx, int vy) {
        return (this.Dot(vx, vy) / this.GetLength());
    }

    public static double Project(Vector2D v1, Vector2D v2) {
        return (Dot(v1, v2) / v1.GetLength());
    }

    // Methoden bzgl. Rotate auskommentiert, da mit int nicht unterstützt
    // Methoden könnten umgeschrieben werden auf cw / ccw in 90 Grad Schritten
    public void Swap() {
        int tmp = x;
        x = y;
        y = tmp;
    }

    public Vector2D GetSwapped() {
        return new Vector2D(y, x);
    }

    public void RotateCW() {
        Swap();
        Multiply(new Vector2D(-1, 1));
    }

    public void RotateCCW() {
        Swap();
        Multiply(new Vector2D(1, -1));
    }

    public Vector2D GetRotatedCW() {
        return new Vector2D(this.GetSwapped().GetMultiplied(new Vector2D(-1, 1)));
    }

    public Vector2D GetRotatedCCW() {
        return new Vector2D(this.GetSwapped().GetMultiplied(new Vector2D(1, -1)));
    }

    public void Reverse() {
        x = -x;
        y = -y;
    }

    public Vector2D GetReversed() {
        return new Vector2D(-x, -y);
    }

    /**
     * Modulus function for Vector 2D. Note: Nothing is done for negative
     * values!
     *
     * @param modulo
     */
    public void Mod(Vector2D modulo) {
        if (modulo.x > 0) {
            this.x = (this.x + modulo.x) % modulo.x;
        }
        if (modulo.y > 0) {
            this.y = (this.y + modulo.y) % modulo.y;
        }
    }

    public Vector2D GetMod(Vector2D range) {
        Vector2D v = this.clone();
        v.Mod(range);
        return v;
    }

    public static Vector2D GetMax(Vector2D v1, Vector2D v2) {
        int xMax = Math.max(v1.x, v2.x);
        int yMax = Math.max(v1.y, v2.y);
        return new Vector2D(xMax, yMax);
    }

    public static Vector2D GetMax(HashSet<Vector2D> vectors) {
        Vector2D maxSoFar = new Vector2D(0, 0);
        for (Vector2D v : vectors) {
            maxSoFar = Vector2D.GetMax(maxSoFar, v);
        }
        return maxSoFar.clone();
    }

    public static Vector2D GetMin(Vector2D v1, Vector2D v2) {
        int xMin = Math.min(v1.x, v2.x);
        int yMin = Math.min(v1.y, v2.y);
        return new Vector2D(xMin, yMin);
    }

    public static Vector2D GetMin(HashSet<Vector2D> vectors) {
        Vector2D minSoFar = new Vector2D(0, 0);
        for (Vector2D v : vectors) {
            minSoFar = Vector2D.GetMin(minSoFar, v);
        }
        return minSoFar.clone();
    }

    /**
     * Extract positions from a hashset of NextMapTiles
     *
     * @param mapTileHashSet HashSet of NextMapTiles
     * @return HashSet of Vector2D
     */
    public static HashSet<Vector2D> ExtractPositionsFromMapTiles(HashSet<NextMapTile> mapTileHashSet) {
        HashSet<Vector2D> vectorHashSet = new HashSet<>();
        for (NextMapTile maptile : mapTileHashSet) {
            vectorHashSet.add(new Vector2D(maptile.GetPosition()));
        }
        return vectorHashSet;
    }

    /**
     * Get the normalized direction of travel
     *
     * @param startPoint beginning point of the travel
     * @param targetPoint end point of the travel
     * @return Vector2D normalised direction
     * @author Alexander Lorenz
     */
    public static Vector2D CalculateNormalisedDirection(Vector2D startPoint, Vector2D targetPoint) {
        int tX = targetPoint.x;
        int tY = targetPoint.y;
        int sX = startPoint.x;
        int sY = startPoint.y;
        int directionX = (tX - sX) / Math.max(Math.abs(tX - sX), 1);
        int directionY = (tY - sY) / Math.max(Math.abs(tY - sY), 1);;
        return new Vector2D(directionX, directionY);
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
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        Vector2D v2 = (Vector2D) o;

        return Objects.equals(x, v2.x) && Objects.equals(y, v2.y);
    }
}
