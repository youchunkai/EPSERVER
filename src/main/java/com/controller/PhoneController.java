package com.controller;

import com.model.ResponseVO;
import com.service.PhoneService;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * @类名： PhoneController
 * @描述: APP2.0 数据接口
 * @作者： zxlei
 * @创建日期： 2020/1/8
 * @版本号： V1.0
 **/
@Controller
@RequestMapping(value="/phone")
@SuppressWarnings("all")
public class PhoneController {

    /**
     * APP2.0 业务层注入
     */
    @Resource(name = "PhoneService")
    PhoneService phoneService;

    /**
     * @方法名: queryUserInf
     * @描述: 查询我的用户信息 用于APP2.0【我的】【个人信息】页面展示
     * @参数: [params, request, response]
     * @返回值: com.model.resultDataObject
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    @RequestMapping(value = "/queryUserInf", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO queryUserInf(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用根据用户编码usCode查询用户信息，用与我的页面展示
            return phoneService.queryUserInf(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"查询用户信息失败！请联系管理员！");
        }

    }

    /**
     * @方法名: updateUserInf
     * @描述: 修改个人信息 用于APP2.0【个人信息】页面使用
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    @RequestMapping(value = "/updateUserInf", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO updateUserInf(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用根据用户编码usCode修改用户信息，用个人信息页面
            return phoneService.updateUserInf(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"修改用户信息失败！请联系管理员！");
        }

    }

    /**
     * @方法名: updateUserPwdInf
     * @描述: 修改密码 用于APP2.0【修改密码】页面使用
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    @RequestMapping(value = "/updateUserPwdInf", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO updateUserPwdInf(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用根据用户编码usCode修改用户密码，修改密码页面
            return phoneService.updateUserPwdInf(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"修改用户密码失败！请联系管理员！");
        }
    }


    /**
     * @方法名: queryQuickFun
     * @描述: 查询用户设置的功能 用于APP2.0【首页】页面使用，可适配：快捷功能4，应用功能9
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/8
    **/
    @RequestMapping(value = "/queryQuickFun", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO queryQuickFun(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用查询快捷应用、应用功能方法
            return phoneService.queryQuickFun(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"查询功能信息失败！请联系管理员！");
        }
    }

    /**
     * @方法名: queryQuickFunAll
     * @描述: 查询所有功能 用于APP2.0【快捷功能】，【应用功能】页面使用
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    @RequestMapping(value = "/queryQuickFunAll", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO queryQuickFunAll(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用查询所有快捷功能、应用功能方法
            return phoneService.queryQuickFunAll(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"查询功能信息失败！请联系管理员！");
        }
    }

    /**
     * @方法名: saveQuickFun
     * @描述: 【快捷功能】，【应用功能】设置保存
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    @RequestMapping(value = "/saveQuickFun", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO saveQuickFun(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用根据用户编码usCode修改用户密码，修改密码页面
            return phoneService.saveQuickFun(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"保存设置功能信息失败！请联系管理员！");
        }
    }


    /**
     * @方法名: queryComponent
     * @描述: 查询用户设置的关注组件 用于APP2.0【首页】页面使用，可适配：关注功能8
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
    **/
    @RequestMapping(value = "/queryComponent", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO queryComponent(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用根据用户编码usCode修改用户密码，修改密码页面
            return phoneService.queryComponent(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"查询关注功能信息失败！请联系管理员！");
        }
    }

    /**
     * @方法名: queryComponentAll
     * @描述: 查询所有用户关注组件 用于APP2.0【我的关注】页面使用
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
     **/
    @RequestMapping(value = "/queryComponentAll", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO queryComponentAll(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用根据用户编码usCode修改用户密码，修改密码页面
            return phoneService.queryComponentAll(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"查询关注功能信息失败！请联系管理员！");
        }
    }

    /**
     * @方法名: saveComponent
     * @描述: 【我的关注】设置保存
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/9
     **/
    @RequestMapping(value = "/saveComponent", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO saveComponent(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用保存关注组件方法
            return phoneService.saveComponent(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"保存设置我的关注失败！请联系管理员！");
        }
    }


    /**
     * @方法名: queryHotFunc
     * @描述: 【热门搜索】展示使用 返回数据量根据传入查询条数【querySize】决定
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/10
    **/
    @RequestMapping(value = "/queryHotFunc", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO queryHotFunc(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用查询热门搜索方法
            return phoneService.queryHotFunc(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"热门搜索失败！请联系管理员！");
        }
    }

    /**
     * @方法名: queryHotFuncByQueryName
     * @描述: 条件查询【热门搜索】使用 返回用户搜索功能
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/10
    **/
    @RequestMapping(value = "/queryHotFuncByQueryName", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO queryHotFuncByQueryName(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用私有方法设置当前用户编码
            HashMap<String, Object> param = setUsCode(params, request);
            // 获取设置后的用户编码
            Object usCode = param.get("usCode");
            // 判断当前用户编码是否为空
            if (usCode == null) {
                // 如果为空返回错误信息
                return ResponseVO.createWithException(400,"您尚未登录！请登陆！");
            }
            // 调用根据条件查询热门搜索方法
            return phoneService.queryHotFuncByQueryName(param);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"条件查询热门搜索失败！请联系管理员！");
        }
    }

    /**
     * @方法名: updateFuncSearchCount
     * @描述: 点击功能时调用，用于更改此项功能查询次数
     * @参数: [params, request, response]
     * @返回值: com.model.ResponseVO
     * @作者: zxlei
     * @日期: 2020/1/10
    **/
    @RequestMapping(value = "/updateFuncSearchCount", method = RequestMethod.POST)
    public @ResponseBody
    ResponseVO updateFuncSearchCount(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 调用修改功能查询次数方法
            return phoneService.updateFuncSearchCount(params);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return ResponseVO.createWithException(400,"修改功能查询次数失败！请联系管理员！");
        }
    }

    /**
     * @方法名: setUsCode
     * @描述: 私有方法，用于设置当前用户编码，将当前用户编码设置到参数集合中
     * @参数: [params, request]
     * @返回值: java.util.HashMap<java.lang.String,java.lang.Object>
     * @作者: zxlei
     * @日期: 2020/1/18
    **/
    private HashMap<String, Object> setUsCode(HashMap<String, Object> params,HttpServletRequest request) {
        // 设置用户编码为空
        String usCode = null;
        // 获取当前session
        HttpSession session = request.getSession();
        // 创建用户集合参数
        HashMap<String, Object> user = null;
        // 判断当前session是否为空
        if(session != null) {
            // 通过session获取当前用户信息
            user = (HashMap<String, Object>) session.getAttribute("user");
        }
        // 判断当前用户信息是否为空
        if(!MapUtils.isEmpty(user)){
            // 获取当前用户编码
            usCode = (String) user.get("US_CODE");
        }
        // 将当前用户编码设置到参数集合中
        params.put("usCode", usCode);
        // 返回存入用户编码参数
        return params;
    }

}
