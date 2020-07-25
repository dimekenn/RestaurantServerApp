package kz.nurbekov.restaurantserver.Common;

import kz.nurbekov.restaurantserver.model.Request;
import kz.nurbekov.restaurantserver.model.User;

public class Common {
    public  static User currentUser;
    public static Request currentRequest;
    public static final String UPDATE = "Изменить";
    public static final String DELETE = "Удалить";

    public static final int PICK_IMAGE_REQUEST =71;

    public static String convertCodeToStatus(String code){
        if (code.equals("0"))
            return "Готовка";
        else if (code.equals("1"))
            return "В пути";
        else
            return "Доставлено!";
    }
}
