interface Drawable{
    public void draw();
}

public class LambdaExpressionExample {
    public static void main(String[] args) {
        int width=10;

        //with lambda
        Drawable d2=()->{
            for (int i = 0; i <5 ; i++) {
                System.out.println("this is to test");
            }
            System.out.println("Drawing "+width);
        };
        d2.draw();
    }
}