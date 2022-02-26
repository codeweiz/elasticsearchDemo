package cn.microboat.domain.vo;

import cn.microboat.domain.User;
import lombok.Data;

/**
 * @author zhouwei
 */
@Data
public class ResultVo {

    private String code;

    private String msg;

    private Integer total;

    private Object data;

    public ResultVo(Object data) {
        this.code = "200";
        this.msg = "SUCCESS";
        this.total = 0;
        this.data = data;
    }
}
