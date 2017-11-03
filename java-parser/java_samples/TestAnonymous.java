abstract class Person{
    abstract void eat();
}
class TestAnonymousInner{
    public static void main(String args[]){
        Person p=new Person(){
            void eat(){
                for (int i = 0; i <5 ; i++) {
                    System.out.println("test if number of loops is captured");
                }
                double a = 2d;
                int c = (int) a;
                System.out.println("nice fruits");}
        };
        p.eat();
    }

    void eat(){
        for (int i = 0; i <5 ; i++) {
            System.out.println("test if number of loops is captured");
        }
        double a = 2d;
        int c = (int) a;
        System.out.println("nice fruits");}
}