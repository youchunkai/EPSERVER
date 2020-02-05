package com.dao.mapper;

import java.util.HashMap;
import java.util.List;

/**
 * @接口名： PhoneMapper
 * @描述: APP2.0 操作数据库接口
 * @作者： zxlei
 * @创建日期： 2020/1/8
 * @版本号： V1.0
 **/
public interface PhoneMapper {
    /**
     * @方法名: queryUserInf
     * @描述: 根据用户编码usCode查询用户信息
     * @参数: [hashMap]
     * @返回值: java.util.List<java.util.HashMap<java.lang.String,java.lang.Object>>
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    public List<HashMap<String,Object>> queryUserInf(HashMap<String, Object> hashMap);

    /**
     * @方法名: updateUserInf
     * @描述: 根据用户编码usCode修改用户信息
     *              可修改：性别，手机号，邮箱，密码
     * @参数: [hashMap]
     * @返回值: void
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    void updateUserInf(HashMap<String, Object> hashMap);

    /**
     * @方法名: queryFunBySysCodeAndFuncType
     * @描述: 根据系统编码，功能类型查询所有功能
     * @参数: [hashMap]
     * @返回值: java.util.List<java.util.HashMap<java.lang.String,java.lang.Object>>
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    public List<HashMap<String,Object>> queryFunBySysCodeAndFuncType(HashMap<String, Object> hashMap);

    /**
     * @方法名: queryUserFunc
     * @描述: 根据系统编码，功能类型查询快捷功能,应用功能
     * @参数: [hashMap]
     * @返回值: java.util.List<java.util.HashMap<java.lang.String,java.lang.Object>>
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    public List<HashMap<String,Object>> queryUserFunc(HashMap<String, Object> hashMap);

    /**
     * @方法名: queryUserRlFunc
     * @描述: 根据系统编码，用户编码，功能类型查询当前用户快捷功能，应用功能
     * @参数: [hashMap]
     * @返回值: java.util.List<java.util.HashMap<java.lang.String,java.lang.Object>>
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    public List<HashMap<String,Object>> queryUserRlFunc(HashMap<String, Object> hashMap);

    /**
     * @方法名: queryUserRlAllFunc
     * @描述: 根据系统编码，用户编码，功能类型查询当前用户所有快捷功能及父级功能，应用功能及父级功能
     * @参数: [hashMap]
     * @返回值: java.util.List<java.util.HashMap<java.lang.String,java.lang.Object>>
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    public List<HashMap<String,Object>> queryUserRlAllFunc(HashMap<String, Object> hashMap);

    /**
     * @方法名: delUserFunc
     * @描述: 根据用户编码，功能类型，功能编码删除数据
     * @参数: [hashMap]
     * @返回值: void
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    void delUserFunc(HashMap<String, Object> hashMap);

    /**
     * @方法名: addUserFunc
     * @描述: 添加用户快捷功能或应用功能
     * @参数: [userFuncList]
     * @返回值: void
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    void addUserFunc(List<HashMap<String, Object>> userFuncList);

    /**
     * @方法名: queryComponent
     * @描述: 根据系统编码，用户编码查询当前用户所有关注组件
     * @参数: [hashMap]
     * @返回值: java.util.List<java.util.HashMap<java.lang.String,java.lang.Object>>
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    public List<HashMap<String,Object>> queryComponent(HashMap<String, Object> hashMap);

    /**
     * @方法名: queryRlComponent
     * @描述: 查询当前用户所有关注组件
     * @参数: [hashMap]
     * @返回值: java.util.List<java.util.HashMap<java.lang.String,java.lang.Object>>
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    public List<HashMap<String,Object>> queryRlComponent(HashMap<String, Object> hashMap);

    /**
     * @方法名: delUserComp
     * @描述: 删除用户设置的关注组件
     * @参数: [hashMap]
     * @返回值: void
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    void delUserComp(HashMap<String, Object> hashMap);

    /**
     * @方法名: addUserComp
     * @描述: 添加用户设置的关注组件
     * @参数: [userFuncList]
     * @返回值: void
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    void addUserComp(List<HashMap<String, Object>> userFuncList);

    /**
     * @方法名: updateFuncSearchCount
     * @描述: 根据功能编码funcCode 修改：查询次数search_count，每次查询次数加1
     * @参数: [hashMap]
     * @返回值: void
     * @作者: zxlei
     * @日期: 2020/1/10
    **/
    void updateFuncSearchCount(HashMap<String, Object> hashMap);

    /**
     * @方法名: queryHotFuncByQueryName
     * @描述: 条件查询【热门搜索】使用 返回用户搜索功能
     * @参数: [hashMap]
     * @返回值: java.util.List<java.util.HashMap<java.lang.String,java.lang.Object>>
     * @作者: zxlei
     * @日期: 2020/1/19
    **/
    public List<HashMap<String,Object>> queryHotFuncByQueryName(HashMap<String, Object> hashMap);

    /**
     * @方法名: queryAllFuncByType
     * @描述: 根据系统编码、功能类型查询所有功能
     * @参数: [hashMap]
     * @返回值: java.util.List<java.util.HashMap<java.lang.String,java.lang.Object>>
     * @作者: zxlei
     * @日期: 2020/1/21
    **/
    public List<HashMap<String,Object>> queryAllFuncByType(HashMap<String, Object> hashMap);
}
