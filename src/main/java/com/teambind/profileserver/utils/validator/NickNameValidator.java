package com.teambind.profileserver.utils.validator;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Setter
public class NickNameValidator {

    @Value("${nickname.validation.regex:^[a-zA-Z0-9_]{3,15}$}")
    private  String regex;

    public  boolean isValidNickName(String nickName) {
         if(!nickName.matches(regex)||nickName.isEmpty()){
             return false;
         };
        return true;
    }
}
