package com.polycis.tcpservice.repository.database;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author : Wenyu Zhou
 * @version : v1.0
 * @date : 2019/8/5
 * description : 描述
 */
@Data
@Entity
@Table(name = "dev_data_up")
public class DevDataUp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceUuid;
    private int platform;
    private String mac;
    private String encodeData;
    private String decodeData;
    private String dataInfo;
    private Date pushTime;
    private int pushStatus;
    private Date reportTime;
    private Date createTime;
    private Date modifyTime;
    private Integer rssi;

}
