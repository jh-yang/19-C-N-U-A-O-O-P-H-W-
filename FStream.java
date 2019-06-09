package stream;

import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
//import java.util.NoSuchElementException;
//import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import stream.FOption;

public interface FStream<T> extends AutoCloseable {
	/**
	* 스트림에 포함된 다음 데이터를 반환한다.
	* <p>
	* 더 이상의 데이터가 없는 경우, 또는 이미 close된 경우에는 {@link FOption#empty()}을 반환함.
	*
	* @return 다음 데이터. 없는 경우는 {@link FOption#empty()}.
	*/
	public FOption<T> next();
	public static <T> FStream<T> empty() {
	// next()가 FOption.empty()를 리턴하게 하는 스트림 객체를 리턴
		return new FStream<T> () {
			@Override
			public void close() throws Exception {}
				
			@Override
			public FOption<T> next() {
				return FOption.empty();
			}
		};
	}
	
	@SafeVarargs
	public static <T> FStream<T> of(T... values) {
		ArrayList<T> list = new ArrayList<T>();
		for(T t:values) {
			list.add(t);	
		}
		// 먼저 values를 임시 저장
		return new FStream<T> () {
			
			Iterator<T> iter = list.iterator(); // 객체 생성시 FStream 내 Field 값으로 values를 iterator 형태로 저장
			
			@Override
			public void close () throws Exception{ 
				iter = list.iterator(); // 포인터를 다시 맨 처음으로 옮김
			}
			
			@Override
			public FOption<T> next() {
				if(iter.hasNext())
					return FOption.of(iter.next()); // next를 부를 때 마다 다음 원소를 가져옴
				else
					return FOption.empty();
			}
		};
		// while을 써야할 수도 있음 , iterator , hasnext
	}
	
	// of와 매우 유사, parameter로 iterable한 값들이 오기 때문에 임시 저장할 필요가 없음
	public static <T> FStream<T> from(Iterable<? extends T> values) {
		Iterator<?> iter1 = values.iterator();
		return new FStream<T> () {
			Iterator<?> iter = iter1;
			@Override
			public void close () throws Exception{
				iter = iter1;
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public FOption<T> next() {
				if(iter.hasNext())
					return FOption.of((T)iter.next());
				else
					return FOption.empty();
			}
		};
	}
	
	// of와 매우 유사, parameter로 iterator가 오기 때문에 임시 저장할 필요가 없음
	public static <T> FStream<T> from(Iterator<? extends T> iter) {
		Iterator<?> iter2 = iter;
		return new FStream<T> () {
			Iterator<?> iter1 = iter2;
			@Override
			public void close () throws Exception{
				iter1 = iter2;
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public FOption<T> next() {
				if(iter.hasNext())
					return FOption.of((T)iter1.next());
				else
					return FOption.empty();
			}
		};
	}
	// Stream의 iterator()를 이용해서 field 값으로 저장
	public static <T> FStream<T> from(Stream<? extends T> stream) {
		return new FStream<T> () {
			Iterator<?> iter = stream.iterator();
			@Override
			public void close () throws Exception{
				iter = stream.iterator();
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public FOption<T> next() {
				if(iter.hasNext())
					return FOption.of((T)iter.next());
				else
					return FOption.empty();
			}
		};
	}

	/**
	* 스트림의 첫 count개의 데이터로 구성된 FStream 객체를 생성한다.
	*
	* @param count 데이터 갯수.
	* @return 'count' 개의 데이터로 구성된 스트림 객체.
	*/
	// 기존 stream에서 count만큼의 원소만 저장하는 새로운 list를 만들고 그것들을 field로 가지는 새로운 FStream 객체 생성
	public default FStream<T> take(long count) {
		ArrayList<T> list1 = this.toList();
		ArrayList<T> list2 = new ArrayList<T>();
		for(int i=0; i<count;i++) {
			if(list1.get(i) == null) // 원소가 없을 시 멈춤
				break;
			list2.add(list1.get(i));
		}
		
		return new FStream<T>() {
			Iterator<T> iter = list2.iterator();
			@Override
			public void close () throws Exception{
				iter = list2.iterator();
			}
			@Override
			public FOption<T> next() {
				if(iter.hasNext())
					return FOption.of(iter.next());
				else
					return FOption.empty();
			}
		};

	// 앞의 n개만 취할 수 있도록 next()를 다시 구현한 클래스의 객체 리턴
	}
	
	// 기존 stream에서 count만큼의 원소를 제외한 나머지 원소를 갖는 새로운 list를 만들고 그 것들을 field로 가지는 새로운 FStream 객체 생성
	public default FStream<T> drop(long count) {
		ArrayList<T> list1 = this.toList();
		ArrayList<T> list2 = new ArrayList<T>();
		for(int i=0; i<list1.size();i++) {
//			if(list1.get(i) == null) // 원소가 없을 시 멈춤
//				break; // 이미 size만큼만 반복하기 때문에 null 검사해줄 필요가 없음
			if(i>=count)
				list2.add(list1.get(i));
		}
		
		return new FStream<T>() {
			Iterator<T> iter = list2.iterator();
			@Override
			public void close () throws Exception{
				iter = list2.iterator();
			}
			@Override
			public FOption<T> next() {
				if(iter.hasNext())
					return FOption.of(iter.next());
				else
					return FOption.empty();
			}
		};
	// n개는 버리고 시작 수 있도록 next()를 다시 구현한 클래스의 객체 리턴
	}
	
	//FStream의 모든 원소에 대해 effect 적용
	public default void forEach(Consumer<? super T> effect) {
		FOption<T> next = FStream.this.next();
		while(next.isPresent()) {
			effect.accept(next.get());
			next = FStream.this.next();
		}
	}
	
	
	public default FStream<T> filter(Predicate<? super T> pred) {		
		return new FStream<T> () {
			@Override
			public void close() throws Exception {}// 여기서는 일단 비워둠
			
			@Override
			public FOption<T> next() {
				FOption<T> next;
				do {
					next = FStream.this.next();
					if (next.isAbsent())
						return FOption.empty();
				} while (!pred.test(next.get()));
				return next;
			}
		};
	}
	
	public default <S> FStream<S> map(Function<? super T,? extends S> mapper) {
		
		ArrayList<T> list1 = this.toList();
		
		return new FStream<S>() {
			ArrayList<T> list = list1;
			int i = 0;
			@Override
			public void close() throws Exception {
				i=0;
			}

			@Override
			public FOption<S> next() {
				if(i < list.size())
					return FOption.of(mapper.apply(list.get(i++)));
				else
					return FOption.empty();
			}
			
		};
	}
	
	// flatMap: 선택 사항
	// 구현 시 추가 점수 있음
//	public default <V> FStream<V> flatMap(Function<? super T,? extends FStream<V>> mapper) {
//	}
	
	//FStream을 iterator 형태로 반환
	public default Iterator<T> iterator() {
		ArrayList<T> list = this.toList();
		return list.iterator();
	}
	
	//FStream을 ArrayList 형태로 반환	
	public default ArrayList<T> toList() {
		ArrayList<T> list = new ArrayList<T>();
		FOption<T> next = this.next();
		while(next.isPresent()) {
			list.add(next.get());
			next = FStream.this.next();
		}
		return list;
	}
	//FStream을 HashSet 형태로 반환
	public default HashSet<T> toSet() {
		HashSet<T> hset = new HashSet<T>();
		FOption<T> next = this.next();
		while(next.isPresent()) {
			hset.add(next.get());
			next = FStream.this.next();
		}
		return hset;
	}
	
	//FStream을 Array 형태로 반환
	@SuppressWarnings("unchecked")
	public default <S> S[] toArray(Class<S> componentType) {
		
		ArrayList<S> list1 = (ArrayList<S>)this.toList();
		ArrayList<S> list = new ArrayList<S>();
		
		for(int i=0;i<list1.size();i++)
			list.add(componentType.cast(list1.get(i))); // 원소를 componenttype으로 바꿔줌
		return (S[]) list.toArray();
	}
	
	//FStream을 Stream 형태로 반환
	public default Stream<T> stream() {
		ArrayList<T> list = this.toList();
		return list.stream();
	// Java의 stream 객체를 리턴
	}
	
	//FStream을 Comparator를 이용해서 정렬함
	public default FStream<T> sort(Comparator<? super T> cmp) {
		
		ArrayList<T> list = this.toList(); // 먼저 원소를 그대로 가져옴
		list.sort(cmp); // compartor를 이용해서 정렬
		
		return FStream.from(list.iterator());
	}
}
