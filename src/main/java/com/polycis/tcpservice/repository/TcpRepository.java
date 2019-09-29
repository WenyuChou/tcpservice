package com.polycis.tcpservice.repository;

import com.polycis.tcpservice.repository.database.DevDataUp;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : Wenyu Zhou
 * @version : v1.0
 * @date : 2019/8/5
 * description : 描述
 */
public interface TcpRepository extends JpaRepository<DevDataUp,Long> {

}
