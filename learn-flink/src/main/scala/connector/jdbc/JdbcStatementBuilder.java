package connector.jdbc;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.util.function.BiConsumerWithException;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@PublicEvolving
public interface JdbcStatementBuilder<T> extends BiConsumerWithException<PreparedStatement, T, SQLException>, Serializable {

}
