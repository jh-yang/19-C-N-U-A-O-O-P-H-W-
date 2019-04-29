import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


public final class FOption<T> {
	
	private final T value;
	private final boolean defined;
	
	private FOption (T value, boolean defined) {
		this.value = value;
		this.defined = defined;
	}
	
	//SFM : yield <value, true>
	public static <T> FOption<T> of(T value){
		if(value == null)
			return empty();
		else
			return new FOption<T>(value,true);
	}
	
	//SFM : yield <null, false>
	public static <T> FOption<T> empty(){
		return new FOption<T>(null,false);
		
	}
	// java optional -> FOption casting
	public static <T> FOption<T> from(Optional<T> opt){
		return of(opt.get());
	}
	
	// if defined is true
	public boolean isPresent() {
		return defined;
	}
	
	// if defined is false
	public boolean isAbsent() {
		return !defined;
	}
	
	// if defined is true ,then return value.
	public T get() {
		if(defined)
			return value;
		else
			throw new NoSuchElementException("Null");
	}
	
	// if defined is true ,then return value. Else, return null;
	public T getOrNull() {
		return defined? value : null;			
	}
	
	// if defined is true, then return value. Else, return elseValue;
	public T getOrElse(T elseValue) {
		return defined? value : elseValue;
	}

	// if defined is true, then return value. Else, return elseSupplier
	public T getOrElse(Supplier<T> elseSupplier) {
		return defined? value : elseSupplier.get();
	}
	
	// if defined is true ,then return value. Else, throws X;
	public <X extends Throwable> T getOrElseThrow(Supplier<X> thrower) throws X{
		if(defined)
			return value;
		else
			throw thrower.get();
	}
	
	// if defined is true ,then effect.accept(value)
	public FOption<T> ifPresent(Consumer<T> effect){
		if(defined)
			effect.accept(value);
		return this;
	}
	
	// if defined is false ,then orElse.run()
	public FOption<T> ifAbsent(Runnable orElse){
		if(!defined)
			orElse.run();
		return this;
	}
	
	// if it satisfy with predicate, then return this.
	public FOption<T> filter(Predicate<T> pred){
		Objects.requireNonNull(pred);
		return test(pred)? this : empty();
	}
	
	public boolean test(Predicate<T> pred) {
		if(defined && pred.test(value))
			return true;
		return false;
	}
	
	public <S> FOption<S> map(Function<T,S> mapper){
		if(defined)
			return FOption.of(mapper.apply(value));
		else
			return FOption.empty();
	}
	
	public String toString() {
		return defined? String.format(" %s ", value) : "FOption.empty";
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof FOption)
			return Objects.equals(this.value, ((FOption<?>) o).value);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

}
