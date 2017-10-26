class Outer_Demo {
    int num;

    // inner class
    private class Inner_Demo {
        public void print() {
            System.out.println("This is an inner class");
            //test if num of loops is captured
            for (int i = 0; i <5 ; i++) {
                System.out.println(i);
            }
        }
        private void test(){
            while (5<0){
                System.out.println("hi");
            }
        }
    }

    // Accessing he inner class from the method within
    void display_Inner() {
        //test num of loops
        while (5>6);
        Inner_Demo inner = new Inner_Demo();
        inner.print();
    }
}

public class My_class {

    public static void main(String args[]) {
        // Instantiating the outer class
        Outer_Demo outer = new Outer_Demo();

        // Accessing the display_Inner() method.
        outer.display_Inner();
    }
}