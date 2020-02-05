package com.service;

import com.dao.mapper.PhoneMapper;
import com.model.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @类名： PhoneService
 * @描述: TODO
 * @作者： zxlei
 * @创建日期： 2020/1/8
 * @版本号： V1.0
 **/
@Service(value = "PhoneService")
public class PhoneService {

    @Autowired
    private PhoneMapper phoneMapper;

    /**
     * @方法名: queryUserInf
     * @描述: TODO 根据用户编码usCode查询用户信息，用与我的页面展示
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    public ResponseVO queryUserInf(HashMap<String,Object> params){
        // 判断参数是否为空
        if (params != null && params.get("usCode") != null && !"".equals(params.get("usCode"))) {
            // 调用查询数据方法查询数据
            List<HashMap<String, Object>> user = phoneMapper.queryUserInf(params);
            // 判断当前用户是否存在
            if (user.size() == 0) {
                // 如果不存再返回错误信息
                return ResponseVO.createWithException(400,"当前用户不存在！查询传入用户参数是否正确！");
            } else {
                // 返回当前用户信息
                return ResponseVO.createOKWithDataWithoutPageinfo("用户信息查询成功！",user.get(0));
            }
        } else {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户参数！");
        }

    }

    /**
     * @方法名: updateUserInf
     * @描述: TODO 修改个人信息 用于APP2.0个人信息页面使用
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    @Transient
    public ResponseVO updateUserInf(HashMap<String,Object> params){
        // 判断参数是否为空
        if (params != null && params.get("usCode") != null && !"".equals(params.get("usCode"))) {
            // 调用查询数据方法查询数据
            phoneMapper.updateUserInf(params);
            // 返回当前用户信息
            return ResponseVO.createOKWithDataWithoutPageinfo("用户信息修改成功！",null);
        } else {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！");
        }
    }

    /**
     * @方法名: updateUserPwdInf
     * @描述: TODO 修改密码 用于APP2.0修改密码页面使用
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    @Transient
    public ResponseVO updateUserPwdInf(HashMap<String,Object> params){
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"参数为空！");
        }
        // 判断用户编码参数是否为空
        if (params.get("usCode") == null || "".equals(params.get("usCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！");
        }
        // 判断旧密码参数是否为空
        if (params.get("usLnpwOld") == null || "".equals(params.get("usLnpwOld"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户旧密码参数！usLnpwOld");
        }
        // 判断新密码参数是否为空
        if (params.get("usLnpw") == null || "".equals(params.get("usLnpw"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户新密码参数！usLnpw");
        }
        // 判断确认新密码参数是否为空
        if (params.get("usLnpwNew") == null || "".equals(params.get("usLnpwNew"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户确认新密码参数！usLnpwNew");
        }
        // 判断传入两次新密码参数是否为一致
        if (!params.get("usLnpw").equals(params.get("usLnpwNew"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"两次输入新密码不一致！");
        }

        // 调用修改数据方法修改数据
        phoneMapper.updateUserInf(params);
        // 返回当前成功结果
        return ResponseVO.createOKWithDataWithoutPageinfo("用户密码修改成功！",null);



    }

    /**
     * @方法名: queryQuickFun
     * @描述: TODO 查询快捷功能、应用功能，返回4个快捷功能或9个应用功能，参数为【用户编码：usCode】【快捷功能类型：ufType】【功能类型：funcType】
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    public ResponseVO queryQuickFun(HashMap<String,Object> params) {
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"参数为空！");
        }
        // 判断用户编码参数是否为空
        if (params.get("usCode") == null || "".equals(params.get("usCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！usCode");
        }
        // 判断用户快捷功能类型参数是否为空
        if (params.get("ufType") == null || "".equals(params.get("ufType"))) {
            return ResponseVO.createWithException(400,"未传入快捷功能类型参数！ufType");
        }
        // 判断用户功能类型参数是否为空
        if (params.get("funcType") == null || "".equals(params.get("funcType"))) {
            return ResponseVO.createWithException(400,"未传入功能类型参数！funcType");
            // 如果功能类型为空，添加查询参数为1功能点，2功能组
        }
        // 声明返回功能个数
        int count;
        if((Integer) params.get("ufType") == 1) {
            count = 4;
        }else if((Integer) params.get("ufType") == 2){
            count = 9;
        } else {
            return ResponseVO.createWithException(400,"传入快捷功能类型参数不存在！ufType");
        }

        // 创建返回集合
        // 1、查询快捷功能表查看当前用户是否设置快捷功能
        List<HashMap<String, Object>> userFuncs = phoneMapper.queryUserFunc(params);
        // 判断当前用户设置快捷功能数量是否为4个
        if (userFuncs.size() == count) {
            // 如果当前用户设置的快捷功能为4个，直接返回结果
            return ResponseVO.createOKWithDataWithoutPageinfo("查询功能成功！",userFuncs);
        }
        // 2、 查询APP2.0当前用户所有功能
        List<HashMap<String, Object>> userRlFuncs = phoneMapper.queryUserRlFunc(params);

        for (int i = 0; i < userRlFuncs.size(); i++){
//            // 判断当前功能是否为空
//            if(userRlFuncs.size() == 0) {
//                break;
//            }
            // 获取当前角色功能
            HashMap<String, Object> userRlFunc = userRlFuncs.get(i);
//            // 判断当前功能是否为空
//            if(userRlFunc == null) {
//                break;
//            }
            // 获取当前角色功能编码
            Object funcCode =  userRlFunc.get("FUNCCODE");
//            userRlFunc.put("ISAUTH","1"); // 设置有权限
            Boolean flag = true;
            for(int j = 0; j < userFuncs.size(); j ++) {
                // 获取当前角色快捷功能
                HashMap<String, Object> userFunc = userFuncs.get(j);
                // 获取当前角色快捷功能编码
                Object funcCodeU =  userFunc.get("FUNCCODE");

                // 判断当前功能是否为快捷功能
                if(funcCode != null && funcCodeU != null && funcCode.equals(funcCodeU)) {
                    flag = false;
                }
            }
            // 如果当前功能不属于快捷功能，添加当前功能到返回集合
            if(flag) {
                userFuncs.add(userRlFunc);
            }
            // 判断当前返回快捷方式为4个 结束循环
            if(userFuncs.size() == count) {
                break;
            }
        }

        // 判断当前用户权限下功能是否满足返回个数
        if (userFuncs.size() < count) {
             // 查询当前系统所有功能
            List<HashMap<String, Object>> allFuncs = phoneMapper.queryAllFuncByType(params);
            for (int i = 0; i < allFuncs.size(); i++){
                // 获取当前角色功能
                HashMap<String, Object> allFunc = allFuncs.get(i);
                // 获取当前角色功能编码
                Object funcCode =  allFunc.get("FUNCCODE");
//                allFunc.put("ISAUTH","0"); // 设置有权限
                Boolean flag = true;
                for(int j = 0; j < userFuncs.size(); j ++) {
                    // 获取当前角色快捷功能
                    HashMap<String, Object> userFunc = userFuncs.get(j);
                    // 获取当前角色快捷功能编码
                    Object funcCodeU =  userFunc.get("FUNCCODE");

                    // 判断当前功能是否为快捷功能
                    if(funcCode != null && funcCodeU != null && funcCode.equals(funcCodeU)) {
                        flag = false;
                    }
                }
                // 如果当前功能不属于快捷功能，添加当前功能到返回集合
                if(flag) {
                    userFuncs.add(allFunc);
                }
                // 判断当前返回快捷方式为4个 结束循环
                if(userFuncs.size() == count) {
                    break;
                }
            }
        }

        // 2、所有APP2.0快捷功能
//        List<HashMap<String, Object>> funcs = phoneMapper.queryFunBySysCodeAndFuncType(params);

        return ResponseVO.createOKWithDataWithoutPageinfo("查询功能成功！",userFuncs);
    }

    /**
     * @方法名: queryAllUserRlFun
     * @描述: TODO 查询所有快捷功能、应用功能，参数为【用户编码：usCode】【快捷功能类型：ufType】【功能类型：funcType】
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    public ResponseVO queryQuickFunAll(HashMap<String,Object> params) {
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"参数为空！params");
        }
        // 判断用户编码参数是否为空
        if (params.get("usCode") == null || "".equals(params.get("usCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！");
        }
        // 判断用户快捷功能类型参数是否为空
        if (params.get("ufType") == null || "".equals(params.get("ufType"))) {
            return ResponseVO.createWithException(400,"未传入快捷功能类型参数！ufType");
        }
        // 判断用户功能类型参数是否为空
        if (params.get("funcType") == null || "".equals(params.get("funcType"))) {
            return ResponseVO.createWithException(400,"未传入功能类型参数！funcType");
        }
        // 调用查询当前用户本级功能及父级功能方法
        List<HashMap<String, Object>> userRlFuncs = phoneMapper.queryUserRlAllFunc(params);
        // 调用查询当前用户设置的快捷功能或应用功能方法
        List<HashMap<String, Object>> userFuncs = phoneMapper.queryUserFunc(params);
        // 返回参数
        List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        // 遍历当前用户所有功能
        for(int i = 0; i < userRlFuncs.size(); i ++) {
            // 获取用户当前功能
            HashMap<String, Object> userRlFunc = userRlFuncs.get(i);
            // 添加当前返回功能是否是用户设置的功能，0为未设置，1为设置
            userRlFunc.put("ISSET",0);
            // 获取当前角色功能编码
            Object funcCode =  userRlFunc.get("FUNCCODE");
            // 声明标记
            Boolean flag = false;
            // 判断当前功能是否为用户设置的快捷功能或应用功能
            for(int j = 0; j < userFuncs.size(); j ++) {
                // 获取用户当前设置的功能
                HashMap<String, Object> userFunc = userFuncs.get(j);
                // 获取当前角色设置的功能编码
                Object funcCodeU =  userFunc.get("FUNCCODE");
                // 判断当前功能是否为用户设置的功能
                if(funcCode != null && funcCodeU != null && funcCode.equals(funcCodeU)) {
                    // 添加当前返回功能是否是用户设置的功能，0为未设置，1为设置
                    userRlFunc.put("ISSET",1);
                }
            }

            // 分类拼接
//            if(userRlFunc.get("FUNCPCODE") == null ) {
//                result.add(userRlFunc);
//                for(int k = 0; k < userRlFuncs.size(); k ++){
//                    if (){
//
//                    }
//                }
//            }

        }
        return ResponseVO.createOKWithDataWithoutPageinfo("查询功能成功！",userRlFuncs);
    }

    /**
     * @方法名: saveQuickFun
     * @描述: TODO 快捷功能，应用功能设置保存，包括添加，删除
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    @Transient
    public ResponseVO saveQuickFun(HashMap<String,Object> params) {
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"参数params为空！");
        }
        // 判断用户编码参数是否为空
        if (params.get("usCode") == null || "".equals(params.get("usCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！");
        }
        // 判断用户快捷功能类型参数是否为空
        if (params.get("ufType") == null || "".equals(params.get("ufType"))) {
            return ResponseVO.createWithException(400,"未传入快捷功能类型参数！ufType");
        }
        // 判断用户快捷功能类型参数是否为空
        if (params.get("funcCode") == null || "".equals(params.get("funcCode"))) {
            return ResponseVO.createWithException(400,"未传入功能编码参数！funcCode");
        }
        // 声明返回功能个数
        int count;
        if((Integer) params.get("ufType") == 1) {
            count = 4;
        }else if((Integer) params.get("ufType") == 2){
            count = 9;
        } else {
            return ResponseVO.createWithException(400,"传入快捷功能类型参数不存在！ufType");
        }
        // 获取当前功能编码
        String funcCodes = (String)params.get("funcCode");
        String[] splitFuncCodes = funcCodes.split(",");
        if(splitFuncCodes.length > count){
            return ResponseVO.createWithException(400,"传入功能编码参数过多！funcCode");
        }
        // 创建添加快捷功能集合
        List<HashMap<String, Object>> userFuncList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0 ; i < splitFuncCodes.length; i ++) {
            // 创建存储当前快捷功能对象集合
            HashMap<String, Object> userFunc = new HashMap<String, Object>();
            // 随机生成uuid
            // 1、uf_code 主键id  varchar2(50)
            String ufCode = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
            userFunc.put("ufCode",ufCode);
            // 2、 us_code用户编码  varchar2(50)
            userFunc.put("usCode",params.get("usCode"));
            // 3、 func_code功能编码 number
            userFunc.put("funcCode",Integer.valueOf(splitFuncCodes[i]));
            // 4、 uf_order功能排序 number
            userFunc.put("ufOrder",i);
            // 5、 分类 1：快捷功能 2：应用功能 integer
            userFunc.put("ufType",params.get("ufType"));
            // 将当前功能添加到快捷功能集合中
            userFuncList.add(userFunc);
        }
        // 删除当前所有设置功能
        phoneMapper.delUserFunc(params);
        // 添加当前设置的功能
        phoneMapper.addUserFunc(userFuncList);
        return ResponseVO.createOKWithDataWithoutPageinfo("保存设置功能成功！","");
    }

    /**
     * @方法名: queryQuickFun
     * @描述: TODO 查询关注功能组件，返回8个功能组件，参数为【用户编码：usCode】
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
     **/
    public ResponseVO queryComponent(HashMap<String,Object> params) {
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"params参数为空！");
        }
        // 判断用户编码参数是否为空
        if (params.get("usCode") == null || "".equals(params.get("usCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！usCode");
        }

        // 创建返回集合
        // 1、查询快捷功能表查看当前用户是否设置快捷功能
        List<HashMap<String, Object>> components = phoneMapper.queryComponent(params);
        // 判断当前用户设置快捷功能数量是否为4个
        if (components.size() == 8) {
            // 如果当前用户设置的快捷功能为4个，直接返回结果
            return ResponseVO.createOKWithDataWithoutPageinfo("查询功能成功！",components);
        }

        // 2、 查询APP2.0当前用户所有功能
        List<HashMap<String, Object>> rlComponents = phoneMapper.queryRlComponent(params);
        for (int i = 0; i < 8; i++){
            // 获取当前角色功能
            HashMap<String, Object> rlComponent = rlComponents.get(i);
            // 获取当前角色功能编码
            Object compCode =  rlComponent.get("COMPCODE");
            Boolean flag = true;
            for(int j = 0; j < components.size(); j ++) {
                // 获取当前角色快捷功能
                HashMap<String, Object> component = components.get(j);
                // 获取当前角色快捷功能编码
                Object compCodeU =  component.get("COMPCODE");
                // 判断当前功能是否为快捷功能
                if(compCode != null && compCodeU != null && compCode.equals(compCodeU)) {
                    flag = false;
                }
            }
            // 如果当前功能不属于快捷功能，添加当前功能到返回集合
            if(flag) {
                components.add(rlComponent);
            }
            // 判断当前返回快捷方式为4个 结束循环
            if(components.size() == 8) {
                break;
            }
        }
        // 2、所有APP2.0快捷功能
//        List<HashMap<String, Object>> funcs = phoneMapper.queryFunBySysCodeAndFuncType(params);

        return ResponseVO.createOKWithDataWithoutPageinfo("查询功能成功！",components);
    }

    /**
     * @方法名: queryComponentAll
     * @描述: TODO 查询所有关注组件【用户编码：usCode】
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
     **/
    public ResponseVO queryComponentAll(HashMap<String,Object> params) {
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"参数为空！");
        }
        // 判断用户编码参数是否为空
        if (params.get("usCode") == null || "".equals(params.get("usCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！");
        }
        // 调用查询当前用户本级功能及父级功能方法
        List<HashMap<String, Object>> rlComponents = phoneMapper.queryRlComponent(params);
        // 调用查询当前用户设置的快捷功能或应用功能方法
        List<HashMap<String, Object>> components = phoneMapper.queryComponent(params);
        // 遍历当前用户所有功能
        for(int i = 0; i < rlComponents.size(); i ++) {
            // 获取用户当前功能
            HashMap<String, Object> rlComponent = rlComponents.get(i);
            // 添加当前返回功能是否是用户设置的功能，0为未设置，1为设置
            rlComponent.put("ISSET",0);
            // 获取当前角色功能编码
            Object compCode = rlComponent.get("COMPCODE");
            // 判断当前功能是否为用户设置的快捷功能或应用功能
            for(int j = 0; j < components.size(); j ++) {
                // 获取用户当前设置的功能
                HashMap<String, Object> component = components.get(j);
                // 获取当前角色设置的功能编码
                Object compCodeU = component.get("COMPCODE");
                // 判断当前功能是否为用户设置的功能
                if(compCode != null && compCodeU != null && compCode.equals(compCodeU)) {
                    // 添加当前返回功能是否是用户设置的功能，0为未设置，1为设置
                    rlComponent.put("ISSET",1);
                }
            }
        }
        return ResponseVO.createOKWithDataWithoutPageinfo("查询功能成功！",rlComponents);
    }

    /**
     * @方法名: saveComponent
     * @描述: TODO 我的关注设置保存，包括添加，删除
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
     **/
    @Transient
    public ResponseVO saveComponent(HashMap<String,Object> params) {
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"参数为空！params");
        }
        // 判断用户编码参数是否为空
        if (params.get("usCode") == null || "".equals(params.get("usCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！usCode");
        }
        // 判断用户关注组件参数是否为空
        if (params.get("compCode") == null || "".equals(params.get("compCode"))) {
            return ResponseVO.createWithException(400,"未传入功能编码参数！compCode");
        }
        // 获取当前功能编码
        String compCodes = (String)params.get("compCode");
        // 拆分组件字符串
        String[] splitCompCodes = compCodes.split(",");
        // 判断传入参数是否大于8
        if(splitCompCodes.length > 8){
            return ResponseVO.createWithException(400,"传入功能编码参数过多！compCode");
        }
        // 创建添加快捷功能集合
        List<HashMap<String, Object>> compList = new ArrayList<HashMap<String, Object>>();
        // 遍历当前需设置的组件编码
        for (int i = 0 ; i < splitCompCodes.length; i ++) {
            // 创建存储当前快捷功能对象集合
            HashMap<String, Object> component = new HashMap<String, Object>();
            // 随机生成uuid
            // 1、uc_code 主键id  varchar2(50)
            String ucCode = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
            component.put("ucCode",ucCode);
            // 2、 us_code用户编码  varchar2(50)
            component.put("usCode",params.get("usCode"));
            // 3、 comp_code组件编码 number
            component.put("compCode",Integer.valueOf(splitCompCodes[i]));
            // 4、 uf_order功能排序 number
            component.put("ucOrder",i);
            // 将当前功能添加到快捷功能集合中
            compList.add(component);
        }
        // 删除当前所有设置功能
        phoneMapper.delUserComp(params);
        // 添加当前设置的功能
        phoneMapper.addUserComp(compList);
        return ResponseVO.createOKWithDataWithoutPageinfo("保存设置功能成功！","");
    }

    /**
     * @方法名: queryhHotFunc
     * @描述: TODO 根据用户输入内容，查询后返回功能点及功能组
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/10
    **/
    public ResponseVO queryHotFunc(HashMap<String,Object> params) {
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"参数为空！");
        }
        // 判断用户编码参数是否为空
        if (params.get("usCode") == null || "".equals(params.get("usCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！usCode");
        }
        // 判断查询结果大小是否为空
        if (params.get("querySize") == null || "".equals(params.get("querySize"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入查询结果大小参数！querySize");
        }
        // 1、查询功能表查看当前用户所有功能
        List<HashMap<String, Object>> components = phoneMapper.queryUserRlFunc(params);
        // 创建返回集合
        List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        // 判断当前用户功能数量是否小于11个
        if (components.size() < (Integer) params.get("querySize")+1) {
            // 如果当前用户设置的快捷功能为4个，直接返回结果
            result.addAll(components);
//            return ResponseVO.createOKWithDataWithoutPageinfo("查询功能成功！",components);
        } else {
            // 设置循环次数，暂时设置为10条
            for (int i = 0; i < (Integer) params.get("querySize"); i ++) {
                // 循环添加用户功能到返回集合中
                result.add(components.get(i));
            }
        }
        return ResponseVO.createOKWithDataWithoutPageinfo("查询功能成功！",result);
    }

    /**
     * @方法名: queryHotFuncByQueryName
     * @描述: TODO 条件查询【热门搜索】使用 返回用户搜索功能
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/10
    **/
    public ResponseVO queryHotFuncByQueryName(HashMap<String,Object> params) {
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"params 参数为空！");
        }
        // 判断用户编码参数是否为空
        if (params.get("usCode") == null || "".equals(params.get("usCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入用户编码参数！usCode");
        }
        // 1、查询功能表查看当前用户所有功能
//        List<HashMap<String, Object>> components = phoneMapper.queryUserRlFunc(params);
        List<HashMap<String, Object>> components = phoneMapper.queryHotFuncByQueryName(params);

        return ResponseVO.createOKWithDataWithoutPageinfo("查询功能成功！",components);
    }

    /**
     * @方法名: updateFuncSearchCount
     * @描述: TODO 根据功能编码funcCode 修改：查询次数，每次查询次数加1
     * @参数: [params]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/10
    **/
    public ResponseVO updateFuncSearchCount(HashMap<String,Object> params) {
        // 判断参数是否为空
        if (params == null) {
            return ResponseVO.createWithException(400,"参数为空！ params");
        }
        // 判断功能编码参数是否为空
        if (params.get("funcCode") == null || "".equals(params.get("funcCode"))) {
            // 返回错误信息
            return ResponseVO.createWithException(400,"未传入功能编码参数！funcCode");
        }
        // 1、修改功能表查询次数功能
        phoneMapper.updateFuncSearchCount(params);

        return ResponseVO.createOKWithDataWithoutPageinfo("修改功能查询次数成功！","");
    }
}
