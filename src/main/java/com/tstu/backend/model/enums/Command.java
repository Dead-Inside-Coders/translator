package com.tstu.backend.model.enums;

public enum Command {
    VAR("Var"), BEGIN("Begin"), END("End"), PRINT("Print"), IF("If"), ENDIF("Endif"),
    THEN("Then"), ELSE("Else"), ELSEIF("Elseif"), WHILE("While"), ENDWHILE("Endwhile"), DO("Do"), CASE("Case"), OF("Of"), ENDCASE("EndCase");

    String name;

    Command(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
