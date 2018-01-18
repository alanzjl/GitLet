package gitlet;

/**
 * command body
 * @author Jialiang
 */
public interface GitBody {

    /**
     * Check grammar.
     * @param args Input arguments.
     * @return Validation.
     */
    boolean check(String... args);

    void run(DataBase db);
}
