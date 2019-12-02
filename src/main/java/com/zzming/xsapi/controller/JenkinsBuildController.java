package com.zzming.xsapi.controller;

import com.zzming.xsapi.model.BaseResponse;
import com.zzming.xsapi.model.JenkinsBuildIdBean;
import com.zzming.xsapi.service.JenkinsBuildIdRecordService;
import com.zzming.xsapi.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/jenkins")
public class JenkinsBuildController {

    @Autowired
    private JenkinsBuildIdRecordService jenkinsBuildIdRecordService;

    @RequestMapping("/hello")
    public String hello() {
        return "Hello,You are my baby!";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String add(@RequestParam(value = "build_id") int build_id, @RequestParam(value = "release_id") String release_id, @RequestParam(value = "type") String type,
                      @RequestParam(value = "version_code") int version_code, @RequestParam(value = "version_name") String version_name) {
        JenkinsBuildIdBean bean = new JenkinsBuildIdBean();
        bean.setBuild_id(build_id);
        bean.setRelease_id(release_id);
        bean.setType(type);
        bean.setVersion_code(version_code);
        bean.setVersion_name(version_name);
        bean.setCreate_time(String.valueOf(System.currentTimeMillis()));
        jenkinsBuildIdRecordService.add(bean);
        return GsonUtil.createJson(new BaseResponse<>(200, "成功", ""));
    }

    @RequestMapping(value = "/find/{buildId}/{type}", method = RequestMethod.GET)
    public void find(@PathVariable("buildId") int buildId, @PathVariable("type") String type, HttpServletResponse response) {
        JenkinsBuildIdBean jenkinsBuildIdBean = jenkinsBuildIdRecordService.find(buildId, type);
        try {
            if (jenkinsBuildIdBean != null) {
                String url = "https://www.xiangshang360.cn";
                if ("xiangshang".equals(jenkinsBuildIdBean.getType())) {
                    url = "https://fir.im/qb7s?release_id=" + jenkinsBuildIdBean.getRelease_id();
                } else if ("blackwhale".equals(jenkinsBuildIdBean.getType())) {
                    url = "https://fir.im/m4zr?release_id=" + jenkinsBuildIdBean.getRelease_id();
                }
                response.sendRedirect(url);
            } else {
                String str = "未查询到BUILD_ID为" + buildId + "的构建成功地址，可能的原因如下：\n1.type类型错误，xiangshang为向上金服，blackwhale为黑鲸智投\n2.buildId值错误或旧的构建无法查询 \n3.该构建分支未合并最新代码";
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                response.getOutputStream().write(str.getBytes("UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
