package com.zzming.xsapi.controller;

import com.zzming.xsapi.model.BaseResponse;
import com.zzming.xsapi.util.FileUtil;
import com.zzming.xsapi.util.GsonUtil;
import com.zzming.xsapi.util.excel.Excel2CsvUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/excel")
public class ExcelController {

    /**
     * excel2csv
     *
     * @return
     */
    @RequestMapping(value = "/excel2csv", method = RequestMethod.GET)
    public void excel2csv(HttpServletResponse response){
        try {
            response.sendRedirect("http://10.200.43.253:8088/xsapi/xs-app.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * excel2csv
     *
     * @return
     */
    @RequestMapping(value = "/excel2csv", method = RequestMethod.POST, produces = "text/html;charset=UTF-8;")
    public String excel2csv(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        assert fileName != null;
        System.out.println("接收到了上传文件：" + fileName);
        String prefix = fileName.substring(0,fileName.lastIndexOf("."));
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String oldName = "【禅道CSV】" + prefix;
        final File lsFile = File.createTempFile(prefix, suffix);
        multipartFile.transferTo(lsFile);
        String csvFilePath = Excel2CsvUtil.excel2csv(lsFile);
        FileUtil.deleteFile(lsFile);
        if(csvFilePath != null){
            return GsonUtil.createJson(new BaseResponse<>(200, "转换成功,开始下载", csvFilePath));
        }else{
            return GsonUtil.createJson(new BaseResponse<>(202, "转换文件失败", ""));
        }
    }

}
