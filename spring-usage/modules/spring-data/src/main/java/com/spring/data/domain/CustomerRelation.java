package com.spring.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Document(indexName = "test", type = "user")
public class CustomerRelation extends BaseDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键自增id
     */
    @Id
    private String id;
    /**
     * 用户pin
     */
    private String userPin;
    /**
     * 账号类型：1.京东PIN 2.微信ID
     */
    private Integer accountType;
    /**
     * 电话
     */
    private String telephone;
    /**
     * 客户姓名
     */
    private String customerName;
    /**
     * 身份证号
     */
    private String idCardNo;
    /**
     * 用户详细地址，包括省市县
     */
    private String address;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 折扣系数，默认0.8
     */
    private Double discountPercent;
    /**
     * 提报揽收员id
     */
    private Integer originTransporterId;
    /**
     * 提报揽收员姓名
     */
    private String originTransporterName;
    /**
     * 当前揽收员id
     */
    private Integer currentStaffNo;
    /**
     * 当前揽收员姓名
     */
    private String currentStaffName;
    /**
     * 用户来源：达达；京东
     */
    private String customerOrigin;
    /**
     * 来源入口：提报；微信公众号；小程序；官网扫码下载；站点
     */
    private String customerEntrance;
    /**
     * 折扣开始日期
     */
    private Date discountBeginTime;
    /**
     * 折扣结束日期（开始日期+365天）
     */
    private Date discountEndTime;
    /**
     * 区域id
     */
    private String orgId;
    /**
     * 区域名称
     */
    private String orgName;
    /**
     * 片区id
     */
    private String areaCode;
    /**
     * 片区名称
     */
    private String areaName;
    /**
     * 分区id
     */
    private String partitionCode;
    /**
     * 分区名称
     */
    private String partitionName;
    /**
     * 所属站点id
     */
    private Integer siteId;
    /**
     * 所属站点名称
     */
    private String siteName;

    /**
     * 时间戳
     */
    private Timestamp ts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserPin() {
        return userPin;
    }

    public void setUserPin(String userPin) {
        this.userPin = userPin;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Integer getOriginTransporterId() {
        return originTransporterId;
    }

    public void setOriginTransporterId(Integer originTransporterId) {
        this.originTransporterId = originTransporterId;
    }

    public String getOriginTransporterName() {
        return originTransporterName;
    }

    public void setOriginTransporterName(String originTransporterName) {
        this.originTransporterName = originTransporterName;
    }

    public Integer getCurrentStaffNo() {
        return currentStaffNo;
    }

    public void setCurrentStaffNo(Integer currentStaffNo) {
        this.currentStaffNo = currentStaffNo;
    }

    public String getCurrentStaffName() {
        return currentStaffName;
    }

    public void setCurrentStaffName(String currentStaffName) {
        this.currentStaffName = currentStaffName;
    }

    public String getCustomerOrigin() {
        return customerOrigin;
    }

    public void setCustomerOrigin(String customerOrigin) {
        this.customerOrigin = customerOrigin;
    }

    public String getCustomerEntrance() {
        return customerEntrance;
    }

    public void setCustomerEntrance(String customerEntrance) {
        this.customerEntrance = customerEntrance;
    }

    public Date getDiscountBeginTime() {
        return discountBeginTime;
    }

    public void setDiscountBeginTime(Date discountBeginTime) {
        this.discountBeginTime = discountBeginTime;
    }

    public Date getDiscountEndTime() {
        return discountEndTime;
    }

    public void setDiscountEndTime(Date discountEndTime) {
        this.discountEndTime = discountEndTime;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getPartitionCode() {
        return partitionCode;
    }

    public void setPartitionCode(String partitionCode) {
        this.partitionCode = partitionCode;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Timestamp getTs() {
        return ts;
    }

    public void setTs(Timestamp ts) {
        this.ts = ts;
    }
}
