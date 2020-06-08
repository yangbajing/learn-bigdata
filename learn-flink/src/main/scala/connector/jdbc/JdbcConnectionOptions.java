package connector.jdbc;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.util.Preconditions;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Optional;

@PublicEvolving
public class JdbcConnectionOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final String url;
    protected final String driverName;
    @Nullable
    protected final String username;
    @Nullable
    protected final String password;

    protected JdbcConnectionOptions(String url, String driverName, String username, String password) {
        this.url = Preconditions.checkNotNull(url, "jdbc url is empty");
        this.driverName = Preconditions.checkNotNull(driverName, "driver name is empty");
        this.username = username;
        this.password = password;
    }

    public String getDbURL() {
        return url;
    }

    public String getDriverName() {
        return driverName;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    /**
     * Builder for {@link JdbcConnectionOptions}.
     */
    public static class JdbcConnectionOptionsBuilder implements Serializable {
        private String url;
        private String driverName;
        private String username;
        private String password;

        public JdbcConnectionOptionsBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public JdbcConnectionOptionsBuilder withDriverName(String driverName) {
            this.driverName = driverName;
            return this;
        }

        public JdbcConnectionOptionsBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public JdbcConnectionOptionsBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public JdbcConnectionOptions build() {
            return new JdbcConnectionOptions(url, driverName, username, password);
        }
    }
}
