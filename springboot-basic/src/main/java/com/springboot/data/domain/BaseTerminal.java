package com.springboot.data.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.util.Date;
import java.util.List;

@ConfigurationProperties("terminal")
public class BaseTerminal {
    @NumberFormat(pattern = "No:#,###")
    private Integer terminalNo;
    private String terminalCode;

    @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date modifyDate;
    private List<BaseLine> lines;
    private List<String> notes;

    public Integer getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(Integer terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getTerminalCode() {
        return terminalCode;
    }

    public void setTerminalCode(String terminalCode) {
        this.terminalCode = terminalCode;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public List<BaseLine> getLines() {
        return lines;
    }

    public void setLines(List<BaseLine> lines) {
        this.lines = lines;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }
}
