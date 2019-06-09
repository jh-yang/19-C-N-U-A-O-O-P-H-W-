package stream;

import java.util.ArrayList;
import java.util.Iterator;

public class StreamTest implements FStream<String>{
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FOption next() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		// 1) of test
		FStream<String> test = FStream.of("hi","bye","plas");
		
		// 2) toList test
		ArrayList<String> list = test.toList(); // toList test
		System.out.println(list);
		test.close();
		// 3) close test
		// iterator가 초기화되어있지 않아서 사용한 후 close()를 통해 초기화해주지 않으면 사용할 수 없다.
		ArrayList<String> list2 = test.toList(); //
		System.out.println(list2);
		test.close();
		
		// 4) from test
		FStream<String> test2 = FStream.from(list.iterator()); // from test
		
		// 5) iterator test
		test2.iterator().forEachRemaining(x -> System.out.println(x));
		test2.close();
		
		// 6) take test
		System.out.println("take test");
		test.take(1).forEach(x -> System.out.println(x)); // take 테스트		
		test.close();
		
		// 7) drop test
		System.out.println("drop test");
		test.drop(1).forEach(x -> System.out.print(x + " ")); // drop test
		test.close();
		
		System.out.println("\nmap test");
		// 8) map test 각 string 끝에 s를 붙이고 프린트
		test.map((x) ->{return x.concat("s");}).forEach(x -> System.out.print(x +" "));
	}
}
