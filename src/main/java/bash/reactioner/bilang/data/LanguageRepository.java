package bash.reactioner.bilang.data;

import java.sql.SQLException;
import java.util.Optional;

public interface LanguageRepository {
    String create(String name);
    Optional<String> read(String name);
    void update(String name, String locale);
    void delete(String name);
    void close() throws SQLException;
}
