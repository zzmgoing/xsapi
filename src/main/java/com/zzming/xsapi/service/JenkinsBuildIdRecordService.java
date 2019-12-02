package com.zzming.xsapi.service;

import com.zzming.xsapi.model.JenkinsBuildIdBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JenkinsBuildIdRecordService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void add(JenkinsBuildIdBean bean) {
        int build_id = bean.getBuild_id();
        String release_id = bean.getRelease_id();
        int version_code = bean.getVersion_code();
        String version_name = bean.getVersion_name();
        String create_time = bean.getCreate_time();
        String type = bean.getType();
        String sql1 = "INSERT INTO zzm_xs_jenkins_build (build_id,release_id,type) VALUES (?,?,?) ON DUPLICATE KEY UPDATE release_id=?";
        int update1 = jdbcTemplate.update(sql1, new Object[]{build_id, release_id, type, release_id});
        String sql2 = "INSERT INTO zzm_xs_apk_new_version (version_code,version_name,release_id,create_time,build_id,type) VALUES (?,?,?,?,?,?)" +
                " ON DUPLICATE KEY UPDATE version_name=?,release_id=?,create_time=?,build_id=?";
        int update2 = jdbcTemplate.update(sql2, new Object[]{version_code, version_name, release_id, create_time, build_id, type, version_name, release_id, create_time, build_id});
        System.out.println("插入结果：" + update1);
        System.out.println("插入结果：" + update2);
    }

    public JenkinsBuildIdBean find(int buildId, String type) {
        String sql = "SELECT * FROM zzm_xs_jenkins_build WHERE build_id=? AND type=?";
        try {
            return jdbcTemplate.<JenkinsBuildIdBean>queryForObject(sql, new Object[]{buildId, type}, new BeanPropertyRowMapper(JenkinsBuildIdBean.class));
        } catch (Exception e) {
            return null;
        }
    }

    public List<JenkinsBuildIdBean> findAll(String type) {
        String sql = "SELECT * FROM zzm_xs_apk_new_version WHERE type=? ORDER BY version_code DESC";
        try {
            return jdbcTemplate.query(sql, new Object[]{type}, new BeanPropertyRowMapper(JenkinsBuildIdBean.class));
        } catch (Exception e) {
            return null;
        }
    }

}