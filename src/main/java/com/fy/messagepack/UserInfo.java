package com.fy.messagepack;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *
 * </p >
 *
 * @author fangyan
 * @since 2020/8/9 16:55
 */
public class UserInfo {
    String userName;
    int age;
    String address;

    public UserInfo() {
    }

    public UserInfo(String userName, int age, String address) {
        this.userName = userName;
        this.age = age;
        this.address = address;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static List<UserInfo> build() {
        List list = new ArrayList();
        int i = 5;
        for (int i1 = 0; i1 < i; i1++) {
            UserInfo user = new UserInfo("user:" + i1, i1 + 1, "address:" + i1);
            list.add(user);
        }
        return list;
    }

    @Override
    public String toString() {
        return String.format("userName:[%s]age:[%s]address:[%s]", this.userName, this.age, this.address);
    }
}
