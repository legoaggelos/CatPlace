package com.legoaggelos.catplace.security.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;

public class SerialBlobDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getText() != null) {
            try {
                return new SerialBlob(Base64.getDecoder().decode(p.getText()));
            } catch (SQLException e) {
                System.out.println("Error deserializing SerialBlob, please report this.\n" + e.getStackTrace());
                return null;
            }
        }
        return null;
    }
}
