package CrazyDiamond.Model;

@FunctionalInterface
public interface ConsumerAvecException<T, E extends Throwable> {
    void accept(T t) throws E;
}
