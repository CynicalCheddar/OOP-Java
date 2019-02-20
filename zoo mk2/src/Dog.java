public class Dog extends Animal {

  //TODO:

  public String eat(Food food) {
      return food.eaten(this);
  }
    public String eaten(Dog dog) {
        return ("dog eats food");
    }
}
