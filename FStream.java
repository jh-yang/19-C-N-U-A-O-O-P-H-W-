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
	* ��Ʈ���� ���Ե� ���� �����͸� ��ȯ�Ѵ�.
	* <p>
	* �� �̻��� �����Ͱ� ���� ���, �Ǵ� �̹� close�� ��쿡�� {@link FOption#empty()}�� ��ȯ��.
	*
	* @return ���� ������. ���� ���� {@link FOption#empty()}.
	*/
	public FOption<T> next();
	public static <T> FStream<T> empty() {
	// next()�� FOption.empty()�� �����ϰ� �ϴ� ��Ʈ�� ��ü�� ����
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
		// ���� values�� �ӽ� ����
		return new FStream<T> () {
			
			Iterator<T> iter = list.iterator(); // ��ü ������ FStream �� Field ������ values�� iterator ���·� ����
			
			@Override
			public void close () throws Exception{ 
				iter = list.iterator(); // �����͸� �ٽ� �� ó������ �ű�
			}
			
			@Override
			public FOption<T> next() {
				if(iter.hasNext())
					return FOption.of(iter.next()); // next�� �θ� �� ���� ���� ���Ҹ� ������
				else
					return FOption.empty();
			}
		};
		// while�� ����� ���� ���� , iterator , hasnext
	}
	
	// of�� �ſ� ����, parameter�� iterable�� ������ ���� ������ �ӽ� ������ �ʿ䰡 ����
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
	
	// of�� �ſ� ����, parameter�� iterator�� ���� ������ �ӽ� ������ �ʿ䰡 ����
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
	// Stream�� iterator()�� �̿��ؼ� field ������ ����
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
	* ��Ʈ���� ù count���� �����ͷ� ������ FStream ��ü�� �����Ѵ�.
	*
	* @param count ������ ����.
	* @return 'count' ���� �����ͷ� ������ ��Ʈ�� ��ü.
	*/
	// ���� stream���� count��ŭ�� ���Ҹ� �����ϴ� ���ο� list�� ����� �װ͵��� field�� ������ ���ο� FStream ��ü ����
	public default FStream<T> take(long count) {
		ArrayList<T> list1 = this.toList();
		ArrayList<T> list2 = new ArrayList<T>();
		for(int i=0; i<count;i++) {
			if(list1.get(i) == null) // ���Ұ� ���� �� ����
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

	// ���� n���� ���� �� �ֵ��� next()�� �ٽ� ������ Ŭ������ ��ü ����
	}
	
	// ���� stream���� count��ŭ�� ���Ҹ� ������ ������ ���Ҹ� ���� ���ο� list�� ����� �� �͵��� field�� ������ ���ο� FStream ��ü ����
	public default FStream<T> drop(long count) {
		ArrayList<T> list1 = this.toList();
		ArrayList<T> list2 = new ArrayList<T>();
		for(int i=0; i<list1.size();i++) {
//			if(list1.get(i) == null) // ���Ұ� ���� �� ����
//				break; // �̹� size��ŭ�� �ݺ��ϱ� ������ null �˻����� �ʿ䰡 ����
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
	// n���� ������ ���� �� �ֵ��� next()�� �ٽ� ������ Ŭ������ ��ü ����
	}
	
	//FStream�� ��� ���ҿ� ���� effect ����
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
			public void close() throws Exception {}// ���⼭�� �ϴ� �����
			
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
	
	// flatMap: ���� ����
	// ���� �� �߰� ���� ����
//	public default <V> FStream<V> flatMap(Function<? super T,? extends FStream<V>> mapper) {
//	}
	
	//FStream�� iterator ���·� ��ȯ
	public default Iterator<T> iterator() {
		ArrayList<T> list = this.toList();
		return list.iterator();
	}
	
	//FStream�� ArrayList ���·� ��ȯ	
	public default ArrayList<T> toList() {
		ArrayList<T> list = new ArrayList<T>();
		FOption<T> next = this.next();
		while(next.isPresent()) {
			list.add(next.get());
			next = FStream.this.next();
		}
		return list;
	}
	//FStream�� HashSet ���·� ��ȯ
	public default HashSet<T> toSet() {
		HashSet<T> hset = new HashSet<T>();
		FOption<T> next = this.next();
		while(next.isPresent()) {
			hset.add(next.get());
			next = FStream.this.next();
		}
		return hset;
	}
	
	//FStream�� Array ���·� ��ȯ
	@SuppressWarnings("unchecked")
	public default <S> S[] toArray(Class<S> componentType) {
		
		ArrayList<S> list1 = (ArrayList<S>)this.toList();
		ArrayList<S> list = new ArrayList<S>();
		
		for(int i=0;i<list1.size();i++)
			list.add(componentType.cast(list1.get(i))); // ���Ҹ� componenttype���� �ٲ���
		return (S[]) list.toArray();
	}
	
	//FStream�� Stream ���·� ��ȯ
	public default Stream<T> stream() {
		ArrayList<T> list = this.toList();
		return list.stream();
	// Java�� stream ��ü�� ����
	}
	
	//FStream�� Comparator�� �̿��ؼ� ������
	public default FStream<T> sort(Comparator<? super T> cmp) {
		
		ArrayList<T> list = this.toList(); // ���� ���Ҹ� �״�� ������
		list.sort(cmp); // compartor�� �̿��ؼ� ����
		
		return FStream.from(list.iterator());
	}
}
