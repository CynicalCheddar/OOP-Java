public class Cat extends Animal {

  //TODO:
  @Override
  public String eat(Food food) {
      return food.eaten(this);
  }

  public String eat(Fruit fruit) {
      return fruit.eaten(this);
  }

}
