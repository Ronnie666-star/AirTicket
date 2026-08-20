package com.ronnie.airTicket.infrastructure.persistence.assembler;

import com.ronnie.airTicket.domain.model.user.User;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.infrastructure.persistence.po.SysUserPO;
import org.springframework.stereotype.Component;

/**
 * PO <-> domain 转换。
 * 注意 PO 和 domain 的字段类型不同（role 是 String，domain 是 UserRole；
 * status 是 Boolean，domain 是 boolean enabled）——这就是"双模型"存在的原因。
 */
@Component
public class SysUserAssembler {

    public User toDomain(SysUserPO po) {
        if (po == null) {
            return null;
        }
        return new User(
                po.getId(),
                po.getUsername(),
                po.getPassword(),
                po.getRealName(),
                po.getAge(),
                po.getEmail(),
                po.getPhone(),
                Boolean.TRUE.equals(po.getStatus()),
                UserRole.valueOf(po.getRole()),
                po.getCreateAt()
        );
    }
}
