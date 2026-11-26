package org.softspace.customer.result;

import org.softspace.customer.enums.CustomerType;
import org.softspace.customer.result.object.CustomerDbResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class ObjectBuilder {

    public static CustomerDbResult getCustomerDbResult(ResultSet rs) throws SQLException {
        Timestamp deletedAt = rs.getTimestamp("deleted_at");

        return new CustomerDbResult(
                rs.getObject("guid", UUID.class),
                rs.getString("name"),
                rs.getString("surname"),
                rs.getString("middle_name"),
                rs.getString("email"),
                rs.getString("phone"),
                CustomerType.valueOf(rs.getString("customer_type")),
                rs.getObject("user_profile_guid", UUID.class),
                rs.getObject("responsible_manager_guid", UUID.class),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                deletedAt != null ? deletedAt.toInstant() : null
        );
    }
}
