package cn.microboat.service;

import cn.microboat.domain.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @author zhouwei
 */
public interface EsUserService extends ElasticsearchRepository<User, Integer> {

    /**
     * 根据 name 查询
     *
     * @param name 姓名
     *
     * @return List User
     * */
    List<User> findByName(String name);

    /**
     * 根据 name 和 info 查询
     *
     * @param name 姓名
     * @param info 信息
     *
     * @return List User
     * */
    List<User> findByNameAndInfo(String name, String info);

}
