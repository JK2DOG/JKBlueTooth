package zc.jk.bluetooth;

import java.io.Serializable;

/**
 * Created by ZhangCheng on 2016/2/14.
 */

public class MeasurementResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private String personId;
    private int _id;
    private int checkShrink;
    private int checkDiastole;
    private int checkHeartRate;
    private int pulse;
    private long createTime;
    private String createTimeStr;
    private String updateTime;
    private String testDataId;
    private String equipType;
    private String equipPid;
    private String checkResult;
    private boolean isUpload;
    private String uploadDate;
    private String medicalRecordRemark;
    private String checkAutoFrom;
    private String checkAutoFlag;
    private String bluetoothName;

    public MeasurementResult() {
    }

    public int get_id() {
        return this._id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getPersonId() {
        return this.personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public int getCheckDiastole() {
        return this.checkDiastole;
    }

    public void setCheckDiastole(int checkDiastole) {
        this.checkDiastole = checkDiastole;
    }

    public int getCheckShrink() {
        return this.checkShrink;
    }

    public void setCheckShrink(int checkShrink) {
        this.checkShrink = checkShrink;
    }

    public int getCheckHeartRate() {
        return this.checkHeartRate;
    }

    public void setCheckHeartRate(int checkHeartRate) {
        this.checkHeartRate = checkHeartRate;
    }

    public int getPulse() {
        return this.pulse;
    }

    public void setPulse(int pulse) {
        this.pulse = pulse;
    }

    public long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getTestDataId() {
        return this.testDataId;
    }

    public void setTestDataId(String testDataId) {
        this.testDataId = testDataId;
    }

    public String getEquipType() {
        return this.equipType;
    }

    public void setEquipType(String equipType) {
        this.equipType = equipType;
    }

    public String getEquipPid() {
        return this.equipPid;
    }

    public void setEquipPid(String equipPid) {
        this.equipPid = equipPid;
    }

    public String getCheckResult() {
        return this.checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    public boolean isUpload() {
        return this.isUpload;
    }

    public void setUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public String getUploadDate() {
        return this.uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getMedicalRecordRemark() {
        return this.medicalRecordRemark;
    }

    public void setMedicalRecordRemark(String medicalRecordRemark) {
        this.medicalRecordRemark = medicalRecordRemark;
    }

    public String getCheckAutoFrom() {
        return this.checkAutoFrom;
    }

    public void setCheckAutoFrom(String checkAutoFrom) {
        this.checkAutoFrom = checkAutoFrom;
    }

    public String getCheckAutoFlag() {
        return this.checkAutoFlag;
    }

    public void setCheckAutoFlag(String checkAutoFlag) {
        this.checkAutoFlag = checkAutoFlag;
    }

    public String toString() {
        return "MeasurementResult [personId=" + this.personId + ", _id=" + this._id + ", checkShrink=" + this.checkShrink + ", checkDiastole=" + this.checkDiastole + ", checkHeartRate=" + this.checkHeartRate + ", pulse=" + this.pulse + ", createTime=" + this.createTime + ", updateTime=" + this.updateTime + ", testDataId=" + this.testDataId + ", equipType=" + this.equipType + ", equipPid=" + this.equipPid + ", checkResult=" + this.checkResult + ", isUpload=" + this.isUpload + ", uploadDate=" + this.uploadDate + ", medicalRecordRemark=" + this.medicalRecordRemark + ", checkAutoFrom=" + this.checkAutoFrom + ", checkAutoFlag=" + this.checkAutoFlag + "]";
    }

    public String getBluetoothName() {
        return this.bluetoothName;
    }

    public void setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    public String getCreateTimeStr() {
        return this.createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }
}
