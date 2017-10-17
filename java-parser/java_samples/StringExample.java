public class StringExample
{	public static void main(String[] args)
	{	String s1 = "Computer Science";
		int x = 307;
		String s2 = s1 + " " + x;
		String s3 = s2.substring(10,17);
		String s4 = "is fun";
		String s5 = s2 + s4;
		
		System.out.println("s1: " + s1);
		System.out.println("s2: " + s2);
		System.out.println("s3: " + s3);
		System.out.println("s4: " + s4);
		System.out.println("s5: " + s5);
		
		//showing effect of precedence
		
		x = 3;
		int y = 5;
		String s6 = x + y + "total";
		String s7 = "total " + x + y;
		String s8 = " " + x + y + "total";
		System.out.println("s6: " + s6);
		System.out.println("s7: " + s7);
		System.out.println("s8: " + s8);
	}
}