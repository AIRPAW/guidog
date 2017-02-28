package ru.scorpds.guidog.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author scorp
 */
public class Image implements RowMapper {

    private String base64;
    private int id;

    public Image(int id, String base64) {
        this.id = id;
        this.base64 = base64;
    }

    public Image() {
    }

    public String getBase64() {
        return base64;
    }

    public int getId() {
        return id;
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Image(rs.getInt("ID"), rs.getString("IMAGE_BASE64"));
    }
}
