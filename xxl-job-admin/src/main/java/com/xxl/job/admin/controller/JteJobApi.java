package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermessionLimit;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.util.PropertiesUtil;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen
 */
@Controller
@RequestMapping("/jte-api")
public class JteJobApi {

    @Resource
    private XxlJobService xxlJobService;


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @PermessionLimit(limit = false)
    public ReturnT<String> add(@RequestBody XxlJobInfo jobInfo, HttpServletRequest request) {

        if (permissionInterceptor(request)) {
            return xxlJobService.add(jobInfo);
        } else {
            return new ReturnT(ReturnT.FAIL_CODE, "权限认定失败");
        }
    }

    private boolean permissionInterceptor(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        return accessToken != null && StringUtils.equals(accessToken, PropertiesUtil.getString("xxl.job.accessToken"));
    }

}
