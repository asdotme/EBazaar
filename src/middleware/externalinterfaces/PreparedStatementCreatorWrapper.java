package middleware.externalinterfaces;

import org.springframework.jdbc.core.PreparedStatementCreator;

public interface PreparedStatementCreatorWrapper extends PreparedStatementCreator {
	Object[] getParams();
}
