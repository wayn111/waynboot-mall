package com.test;

public class FoodTest {

    class Food {
    }

    class Fruit extends Food {
    }

    class Apple extends Fruit {
    }

    class Origin extends Fruit {
    }

    class Plate <T>{
        private T item;

        public Plate(T t) {
            item = t;
        }

        public void set(T t) {
            item = t;
        }

        public T get() {
            return item;
        }
    }

    public static void main(String[] args) {
        new FoodTest().test();
    }

    public void test() {
        Plate<? extends Fruit> p = new Plate<>(new Apple());
    }
}
