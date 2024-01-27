package bash.reactioner.bilang.data;

import bash.reactioner.bilang.BiLangPlugin;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.Optional;

public class MySqlLanguageRepository implements LanguageRepository {
    private String url;
    private String username;
    private String password;
    private Connection connection;
    private BiLangPlugin main;

    public MySqlLanguageRepository(BiLangPlugin main, String url, String username, String password) throws SQLException, ClassNotFoundException {
        this.url = url;
        this.main = main;
        this.username = username;
        this.password = password;
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(url, username, password);
        if (main.isShouldCreateTableIfNotExists()) {
            try (Statement statement = connection.createStatement()){
                statement.execute(main.getCreateTableSql());
            }
        }
    }

    @Override
    public String create(String name) {
        try (PreparedStatement statement = connection.prepareStatement(main.getCreateSql())) {
            statement.setString(1, name);
            statement.setString(2, main.getDefaultLocale());
            statement.execute();
            return main.getDefaultLocale();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> read(String name) {
        try (PreparedStatement statement = connection.prepareStatement(main.getReadSql())) {
            statement.setString(1, name);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                if (set.getString(1) == null) {
                    update(name, main.getDefaultLocale());
                    return Optional.of(main.getDefaultLocale());
                }
                else return Optional.of(set.getString(1));
            }
            else return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String name, String locale) {
        try (PreparedStatement statement = connection.prepareStatement(main.getUpdateSql())) {
            statement.setString(1, locale);
            statement.setString(2, name);

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String name) {
        try (PreparedStatement statement = connection.prepareStatement(main.getDeleteSql())) {
            statement.setString(1, name);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
