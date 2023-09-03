package CrazyDiamond.Model;

@FunctionalInterface
public interface FunctionAvecException <T extends Object, R,  E extends Throwable> {
    R apply(T t) throws E ;
}
