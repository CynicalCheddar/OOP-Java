public class Animal {

  public String eat(Food food) {
     return food.eaten(this);
  }

  public String eat(Fruit fruit) {
    return fruit.eaten(this);
  }

  public String eat(Chocolate chocolate) {
     return chocolate.eaten(this);
  }

}
