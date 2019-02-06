

public class Vector2D {

    private double x1 = 1;
    private double x2 = 1;

    double getX1(){
        return x1;
    }
    double getX2(){
        return x2;
    }

    void setX1(double newX1){
        x1 = newX1;
    }
    void setX2(double newX2){
        x2 = newX2;
    }

    double distance(Vector2D v){
        return (Math.sqrt((x1-v.getX1())*(x1-v.getX1()) +(x2-v.getX2())*(x2-v.getX2())));
    }

    double magnitude(){
        return (Math.sqrt(x1*x1 + x2*x2));
    }

    Vector2D add(Vector2D v){
        Vector2D addVector = new Vector2D();
        addVector.setX1(x1 + v.getX1());
        addVector.setX2(x2 + v.getX2());
    }

    Vector2D scale(double f){
        Vector2D scaleVector = new Vector2D();
        scaleVector.setX1(x1*f);
        scaleVector.setX2(x2*f);
        return scaleVector;
    }

    Vector2D midpoint(Vector2D v){
        Vector2D midpointVector = new Vector2D();
        midpointVector.setX1((x1 + v.x1)/2);
        midpointVector.setX2((x2 + v.x2)/2);
        return midpointVector;
    }

    double dotProduct(Vector2D v){
        return (x1 * v.x1 + x2 * v.x2);
    }
}
