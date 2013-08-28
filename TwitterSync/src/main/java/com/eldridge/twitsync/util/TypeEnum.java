package com.eldridge.twitsync.util;

/**
 * Created by reldridge1 on 8/28/13.
 */
public enum TypeEnum {

    PHOTO("photo");

    private TypeEnum(String _type) {
        this.type = _type;
    }

    public String type;

    public static TypeEnum parse(TypeEnum type) { return type; }

    public static TypeEnum parse(String input) {
        //return TypeEnum.valueOf(input);
        if (input != null) {
            for (TypeEnum b : TypeEnum.values()) {
                if (b.type.equalsIgnoreCase(input)) {
                    return b;
                }
            }
        }
        return null;
    }

}
