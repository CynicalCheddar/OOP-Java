public class Cat extends Animal {

    public String eat(Food food) {
        return food.eaten(this);
    }
    public String eaten(Cat cat) {
        return ("cat eats food");
    }

}
